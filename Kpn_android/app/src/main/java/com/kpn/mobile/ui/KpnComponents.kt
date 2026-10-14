package com.kpn.mobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kpn.mobile.R
import com.kpn.mobile.core.Result

internal val LocalMemberShell = androidx.compose.runtime.staticCompositionLocalOf { false }

// ── Brand Top App Bar ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KpnTopBar(
    title: String,
    back: (() -> Unit)? = null,
    hasUnread: Boolean = false,
    onNotification: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    if (LocalMemberShell.current) {
        Row(Modifier.fillMaxWidth().heightIn(min=56.dp).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically) {
            if(back!=null)IconButton(onClick=back){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back")}
            Text(title,style=MaterialTheme.typography.titleLarge,modifier=Modifier.weight(1f).padding(start=if(back==null)12.dp else 4.dp))
            actions()
        }
        return
    }
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.kpn_logo),
                    contentDescription = "KPN emblem",
                    modifier = Modifier.size(32.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        },
        navigationIcon = {
            if (back != null) {
                IconButton(onClick = back) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            actions()
            if (onNotification != null) {
                Box {
                    IconButton(onClick = onNotification) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                    if (hasUnread) {
                        Badge(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

// ── Section Header ─────────────────────────────────────────────────────────────

@Composable
internal fun DashSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        content()
    }
}

// ── Live Metric Display ────────────────────────────────────────────────────────

@Composable
internal fun LiveMetricPair(
    leftValue: String,
    leftLabel: String,
    rightValue: String,
    rightLabel: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    ) {
        LiveMetricCard(leftValue, leftLabel, Modifier.weight(1f))
        LiveMetricCard(rightValue, rightLabel, Modifier.weight(1f))
    }
}

@Composable
internal fun LiveMetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(12.dp)) {
            val isLive = value.isBlank() || value == "Live"
            Text(
                text = if (isLive) "Not available" else value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = if (isLive) 14.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

// ── Dashboard List Row (with letter avatar) ────────────────────────────────────

@Composable
internal fun DashRow(
    letter: String,
    title: String,
    subtitle: String,
    status: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .heightIn(min = 48.dp).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = letter.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        status?.let { StatusBadge(it) }
        if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

// ── Status Badge ───────────────────────────────────────────────────────────────

@Composable
internal fun StatusBadge(text: String) {
    val (color, background) = statusColors(statusTone(text))
    Surface(color = background, shape = MaterialTheme.shapes.extraSmall) {
        Text(
            text = text.replace('_', ' '),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

// ── Info Banner ────────────────────────────────────────────────────────────────

internal enum class BannerTone { INFO, WARNING, SUCCESS }

/** Existing isWarning callers remain source-compatible. */
@Composable
internal fun InfoBanner(text: String, isWarning: Boolean = false, tone: BannerTone = if (isWarning) BannerTone.WARNING else BannerTone.INFO) {
    val (foreground, background) = statusColors(when(tone) {
        BannerTone.INFO -> StatusTone.INFO
        BannerTone.WARNING -> StatusTone.WARNING
        BannerTone.SUCCESS -> StatusTone.SUCCESS
    })
    Surface(color = background, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(when(tone) { BannerTone.WARNING -> Icons.Default.Warning; BannerTone.SUCCESS -> Icons.Default.CheckCircle; else -> Icons.Default.Info },
                contentDescription = null, tint = foreground, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = foreground)
        }
    }
}

// ── Error Components ───────────────────────────────────────────────────────────

@Composable
internal fun ErrorBar(message: String, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(8.dp))
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Dismiss") }
        }
    }
}

@Composable
internal fun ErrorState(error: Result.Err?, onDismiss: () -> Unit) {
    error?.let {
        Column {
            ErrorBar(it.message, onDismiss)
            it.fields.forEach { (field, msg) ->
                Text(
                    "${field.replace('_', ' ').replaceFirstChar(Char::uppercase)}: $msg",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                )
            }
        }
    }
}

// ── Loading ────────────────────────────────────────────────────────────────────

@Composable
internal fun Loading(show: Boolean) {
    if (show) LinearProgressIndicator(Modifier.fillMaxWidth())
}

// ── Empty State ────────────────────────────────────────────────────────────────

@Composable
internal fun EmptyState(text: String, icon: ImageVector = Icons.Default.Inbox) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

}

// ── Centered Splash ────────────────────────────────────────────────────────────

@Composable
internal fun CenteredMessage(text: String, button: String? = null, onClick: () -> Unit = {}) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Image(painterResource(R.drawable.kpn_logo), null, Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(text, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            if (button != null) {
                Spacer(Modifier.height(16.dp))
                Button(shape=MaterialTheme.shapes.small, modifier=Modifier.heightIn(min=48.dp), onClick = onClick) { Text(button) }
            }
        }
    }
}

// ── KPN Text Field ─────────────────────────────────────────────────────────────

@Composable
internal fun KpnField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    lines: Int = 1,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    error: String? = null,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp),
        singleLine = lines == 1,
        minLines = lines,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = keyboardOptions,
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = trailingIcon ?: if (password) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        if (visible) "Hide" else "Show",
                    )
                }
            }
        } else null,
    )
}

// ── KPN Dropdown Field ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> KpnDropdown(
    label: String,
    selected: T?,
    items: List<T>,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it },modifier=modifier) {
        OutlinedTextField(
            shape = MaterialTheme.shapes.small,
            value = selected?.let(itemLabel) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable).padding(bottom = 12.dp),
            enabled = enabled,
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item)) },
                    onClick = { onSelect(item); expanded = false },
                )
            }
        }
    }
}

// ── Divider Row ────────────────────────────────────────────────────────────────

@Composable
internal fun KpnDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

// ── Avatar (photo or initials) ─────────────────────────────────────────────────

@Composable
internal fun KpnAvatar(photoUrl: String?, name: String, size: Int = 48) {
    var imageFailed by remember(photoUrl) { mutableStateOf(false) }
    if (!photoUrl.isNullOrBlank() && !imageFailed) {
        AsyncImage(
            model = photoUrl,
            onError = { imageFailed = true },
            contentDescription = name,
            modifier = Modifier.size(size.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Surface(
            modifier = Modifier.size(size.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) }.uppercase().ifBlank { "K" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
