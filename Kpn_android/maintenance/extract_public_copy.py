#!/usr/bin/env python3
"""Versioned, static-only website copy extraction. Never emits dynamic records or bank values."""
import argparse,hashlib,json,re
from pathlib import Path
from bs4 import BeautifulSoup
parser=argparse.ArgumentParser();parser.add_argument('backend',type=Path);parser.add_argument('output',type=Path);args=parser.parse_args();args.output.mkdir(parents=True,exist_ok=True)
LINKS={'core:contact':'https://www.kpn.com.ng/contact/','staff:register':'https://www.kpn.com.ng/register/','campaigns:newsroom':'https://www.kpn.com.ng/newsroom/','core:advocacy':'https://www.kpn.com.ng/advocacy/'}
def norm(x):return re.sub(r'\s+',' ',x).strip()
def clean_html(node):
 soup=BeautifulSoup(str(node),'html.parser')
 for t in soup.find_all(True):
  attrs={'href':t.get('href')} if t.name=='a' and t.get('href','').startswith(('https://','http://')) else {}
  t.attrs=attrs
 for t in soup.find_all(['i','script','style','button','img']):t.decompose()
 return str(soup)
def block(node):
 k='heading' if re.fullmatch(r'h[1-6]',node.name) else 'link' if node.name=='a' else 'body'
 h=clean_html(node);text=norm(BeautifulSoup(h,'html.parser').get_text(' ',strip=True))
 if not text:return None
 if node.name=='li':
  siblings=node.parent.find_all('li',recursive=False);prefix=f'{siblings.index(node)+1}. ' if node.parent.name=='ol' else '• '
  h=prefix+clean_html(node)
 return {'kind':k,'html':h,'text':text}
manifest={}
for name in ['home','about','code_of_conduct','faq','support_us']:
 p=args.backend/'core/templates/core'/f'{name}.html';raw=p.read_text();src=raw.split('{% block content %}',1)[1].split('{% endblock %}',1)[0]
 src=re.sub(r"{%\s*url\s+'([^']+)'\s*%}",lambda m:LINKS.get(m[1],''),src)
 # Home's two record sections are dynamic: never bundle campaign/news placeholders.
 if name=='home':src=src.split('<!-- Featured Campaigns -->')[0]+src.split('<!-- Call to Action -->')[1]
 src=re.sub(r'{%.*?%}|{{.*?}}','',src,flags=re.S)
 soup=BeautifulSoup(src,'html.parser');page={'version':1,'source':str(p.relative_to(args.backend)),'source_sha256':hashlib.sha256(raw.encode()).hexdigest(),'blocks':[]}
 if name=='faq':
  faq=soup.select('.faq-item');page['categories']=[norm(n.get_text(' ',strip=True)) for n in soup.select('.category-btn')]
  page['questions']=[{'category':n['data-category'],'question':norm(n.select_one('button span').get_text(' ',strip=True)),'answer':[b for x in n.select('div[x-show] p, div[x-show] li') if (b:=block(x))]} for n in faq]
  for n in faq:n.decompose()
  for n in soup.select('.category-btn'):n.decompose()
 if name=='support_us':
  # Bank labels and their static-looking values are not canonical narrative. API state alone governs them.
  for label in ['Account Name','Bank Name','Account Number']:
   n=next((n for n in soup.find_all('p') if norm(n.get_text())==label),None)
   if n:n.parent.decompose()
 for n in soup.find_all(re.compile(r'^h[1-6]$') )+[]:pass
 # Preserve document order; only leaf prose/list/link items, not surrounding desktop containers.
 for n in soup.find_all(['h1','h2','h3','h4','h5','h6','p','li','a']):
  if n.find_parent(['p','li']):continue
  if b:=block(n):page['blocks'].append(b)
 key={'code_of_conduct':'conduct','support_us':'support'}.get(name,name)
 (args.output/f'{key}.json').write_text(json.dumps(page,ensure_ascii=False,indent=2)+'\n')
 manifest[key]={'source':page['source'],'sha256':page['source_sha256'],'blocks':len(page['blocks']),'questions':len(page.get('questions',[]))}
 print(key,len(page['blocks']),'blocks',len(page.get('questions',[])),'questions')
(args.output/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
