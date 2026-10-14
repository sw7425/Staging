package com.kpn.mobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.kpn.mobile.core.Result
import com.kpn.mobile.data.Member

/** One active confirmation surface: progress, exact server error and retry stay visible here. */
@Composable internal fun MemberDecisionDialog(member: Member, action: String, busy: Boolean, error: Result.Err?, onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest={if(!busy)onCancel()},
        title={Text(if(action=="approve")"Approve membership?" else "Reject and archive application?")},
        text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(if(action=="approve")"Approve ${member.name} for ${member.role_title ?: member.role}? The server will recheck jurisdiction and seat availability." else "Reject and archive ${member.name}'s application? Sign-in will be disabled. The applicant account and related records will be preserved, not deleted.")
            if(busy) { LinearProgressIndicator(Modifier.fillMaxWidth());Text("Submitting decision…",style=MaterialTheme.typography.bodySmall) }
            error?.let { Surface(color=MaterialTheme.colorScheme.errorContainer,shape=MaterialTheme.shapes.small) {
                Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Text(it.message,color=MaterialTheme.colorScheme.onErrorContainer)
                    it.fields.values.distinct().filter{field->field!=it.message}.forEach{field->Text(field,color=MaterialTheme.colorScheme.onErrorContainer,style=MaterialTheme.typography.bodySmall)}
                    it.status?.let{status->Text("Response $status · ${it.code.replace('_',' ')}",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onErrorContainer)}
                }
            } }
        }},
        confirmButton={TextButton(enabled=!busy,onClick=onConfirm,modifier=Modifier.heightIn(min=48.dp).testTag("member-decision-confirm")) {
            Text(if(busy)"Please wait…" else if(error!=null)"Retry ${if(action=="approve")"approval" else "rejection"}" else if(action=="approve")"Approve" else "Reject & archive",color=if(busy)MaterialTheme.colorScheme.onSurface.copy(alpha=.38f) else if(action=="reject")MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }},
        dismissButton={TextButton(enabled=!busy,onClick=onCancel,modifier=Modifier.heightIn(min=48.dp)){Text("Cancel")}},
    )
}
