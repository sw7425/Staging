package com.kpn.mobile.core
import com.kpn.mobile.data.UserDto
fun accessContextKey(u:UserDto):List<Any?> =listOf(u.id,u.status,u.tier,u.role_definition,u.dashboard,u.zone,u.lga,u.ward,u.capabilities,u.mobile_tools,u.access)
fun sameAccessContext(a:UserDto,b:UserDto):Boolean=accessContextKey(a)==accessContextKey(b)
