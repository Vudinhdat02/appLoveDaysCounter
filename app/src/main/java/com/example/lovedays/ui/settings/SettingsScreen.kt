package com.example.lovedays.ui.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lovedays.R
import com.example.lovedays.core.ui.theme.accentPreview
import com.example.lovedays.core.ui.theme.supportsDynamicColor
import com.example.lovedays.data.datastore.AccentColor
import com.example.lovedays.data.datastore.CoupleSettings
import com.example.lovedays.data.datastore.ThemeMode
import com.example.lovedays.ui.components.Avatar
import com.example.lovedays.ui.components.LoveDatePickerDialog
import com.example.lovedays.ui.components.MilestoneText
import com.example.lovedays.ui.onboarding.NameField
import androidx.compose.ui.text.input.ImeAction

private enum class SettingsDialog { NAMES, START_DATE, TIME, RESET_1, RESET_2, AVATAR_ME, AVATAR_PARTNER }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(contentPadding: PaddingValues, viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val s = settings ?: return
    val context = LocalContext.current
    var dialog by rememberSaveable { mutableStateOf<SettingsDialog?>(null) }
    var language by remember { mutableStateOf(viewModel.currentLanguage()) }

    var systemNotificationsAllowed by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        systemNotificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        onPauseOrDispose { }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        systemNotificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val imageRequest = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    val pickMine = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::pickMyAvatar)
    }
    val pickPartner = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::pickPartnerAvatar)
    }

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        containerColor = Color.Transparent,
        topBar = { TopAppBar(colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent), title = { Text(stringResource(R.string.settings_title)) }) },
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // --- Cặp đôi ---
            SectionHeader(stringResource(R.string.settings_section_couple))
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarButton(s.myAvatarPath, s.myName) { dialog = SettingsDialog.AVATAR_ME }
                AvatarButton(s.partnerAvatarPath, s.partnerName) { dialog = SettingsDialog.AVATAR_PARTNER }
            }
            ListItem(
        colors = transparentListItem(),
                modifier = Modifier.clickable { dialog = SettingsDialog.NAMES },
                headlineContent = { Text(stringResource(R.string.settings_names)) },
                supportingContent = { Text("${s.myName} ❤ ${s.partnerName}") },
            )
            ListItem(
        colors = transparentListItem(),
                modifier = Modifier.clickable { dialog = SettingsDialog.START_DATE },
                headlineContent = { Text(stringResource(R.string.settings_start_date)) },
                supportingContent = { Text(s.startDate?.let { MilestoneText.date(it) }.orEmpty()) },
            )
            SwitchItem(
                title = stringResource(R.string.settings_day_one),
                subtitle = stringResource(R.string.settings_day_one_desc),
                checked = s.countStartAsDayOne,
                onChange = viewModel::setCountStartAsDayOne,
            )
            HorizontalDivider()

            // --- Thông báo ---
            SectionHeader(stringResource(R.string.settings_section_notifications))
            SwitchItem(
                title = stringResource(R.string.settings_notifications),
                subtitle = null,
                checked = s.notificationsEnabled,
                onChange = { enabled ->
                    viewModel.setNotificationsEnabled(enabled)
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !systemNotificationsAllowed) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )
            if (s.notificationsEnabled && !systemNotificationsAllowed) {
                ListItem(
        colors = transparentListItem(),
                    modifier = Modifier.clickable {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                        )
                    },
                    headlineContent = {
                        Text(stringResource(R.string.settings_notifications_blocked), color = MaterialTheme.colorScheme.error)
                    },
                    supportingContent = { Text(stringResource(R.string.settings_notifications_open_system)) },
                )
            }
            ListItem(
        colors = transparentListItem(),
                modifier = Modifier.clickable(enabled = s.notificationsEnabled) { dialog = SettingsDialog.TIME },
                headlineContent = { Text(stringResource(R.string.settings_reminder_time)) },
                supportingContent = { Text("%02d:%02d".format(s.reminderHour, s.reminderMinute)) },
            )
            SwitchItem(
                title = stringResource(R.string.settings_remind_one_day),
                subtitle = null,
                checked = s.remindOneDayBefore,
                enabled = s.notificationsEnabled,
                onChange = viewModel::setRemindOneDayBefore,
            )
            SwitchItem(
                title = stringResource(R.string.settings_remind_seven_days),
                subtitle = null,
                checked = s.remindSevenDaysBefore,
                enabled = s.notificationsEnabled,
                onChange = viewModel::setRemindSevenDaysBefore,
            )
            HorizontalDivider()

            // --- Giao diện ---
            SectionHeader(stringResource(R.string.settings_section_appearance))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val modes = listOf(
                    ThemeMode.LIGHT to R.string.theme_light,
                    ThemeMode.DARK to R.string.theme_dark,
                    ThemeMode.SYSTEM to R.string.theme_system,
                )
                modes.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = s.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                    ) { Text(stringResource(label), maxLines = 1) }
                }
            }
            Text(
                stringResource(R.string.settings_accent),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AccentColor.entries
                    .filter { it != AccentColor.DYNAMIC || supportsDynamicColor }
                    .forEach { accent ->
                        AccentSwatch(accent, selected = s.accentColor == accent) { viewModel.setAccentColor(accent) }
                    }
            }
            Spacer(Modifier.size(8.dp))
            HorizontalDivider(Modifier.padding(top = 8.dp))

            // --- Ngôn ngữ ---
            SectionHeader(stringResource(R.string.settings_section_language))
            AppLanguage.entries.forEach { lang ->
                ListItem(
        colors = transparentListItem(),
                    modifier = Modifier.selectable(
                        selected = language == lang,
                        role = Role.RadioButton,
                        onClick = {
                            language = lang
                            viewModel.setLanguage(lang)
                        },
                    ),
                    headlineContent = {
                        Text(stringResource(if (lang == AppLanguage.VIETNAMESE) R.string.language_vi else R.string.language_en))
                    },
                    leadingContent = { RadioButton(selected = language == lang, onClick = null) },
                )
            }
            HorizontalDivider()

            // --- Dữ liệu ---
            SectionHeader(stringResource(R.string.settings_section_data))
            ListItem(
        colors = transparentListItem(),
                modifier = Modifier.clickable { dialog = SettingsDialog.RESET_1 },
                headlineContent = { Text(stringResource(R.string.settings_reset), color = MaterialTheme.colorScheme.error) },
                supportingContent = { Text(stringResource(R.string.settings_reset_desc)) },
            )
            Spacer(Modifier.size(24.dp))
        }
    }

    when (dialog) {
        SettingsDialog.NAMES -> NamesDialog(s, onDismiss = { dialog = null }) { a, b ->
            viewModel.setNames(a, b)
            dialog = null
        }
        SettingsDialog.START_DATE -> LoveDatePickerDialog(
            initial = s.startDate,
            maxDate = viewModel.today,
            onDismiss = { dialog = null },
            onConfirm = {
                viewModel.setStartDate(it)
                dialog = null
            },
        )
        SettingsDialog.TIME -> TimeDialog(s.reminderHour, s.reminderMinute, onDismiss = { dialog = null }) { h, m ->
            viewModel.setReminderTime(h, m)
            dialog = null
        }
        SettingsDialog.AVATAR_ME, SettingsDialog.AVATAR_PARTNER -> {
            val mine = dialog == SettingsDialog.AVATAR_ME
            val hasPhoto = if (mine) s.myAvatarPath != null else s.partnerAvatarPath != null
            AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text(if (mine) s.myName else s.partnerName) },
                text = { Text(stringResource(R.string.settings_avatar_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        if (mine) pickMine.launch(imageRequest) else pickPartner.launch(imageRequest)
                        dialog = null
                    }) { Text(stringResource(R.string.action_choose_photo)) }
                },
                dismissButton = {
                    if (hasPhoto) {
                        TextButton(onClick = {
                            if (mine) viewModel.removeMyAvatar() else viewModel.removePartnerAvatar()
                            dialog = null
                        }) { Text(stringResource(R.string.action_remove_photo)) }
                    } else {
                        TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.action_cancel)) }
                    }
                },
            )
        }
        SettingsDialog.RESET_1 -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(R.string.reset_title_1)) },
            text = { Text(stringResource(R.string.reset_message_1)) },
            confirmButton = {
                TextButton(onClick = { dialog = SettingsDialog.RESET_2 }) { Text(stringResource(R.string.action_continue)) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
        SettingsDialog.RESET_2 -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(R.string.reset_title_2)) },
            text = { Text(stringResource(R.string.reset_message_2)) },
            confirmButton = {
                TextButton(onClick = {
                    dialog = null
                    viewModel.resetAll()
                }) { Text(stringResource(R.string.reset_confirm), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
        null -> Unit
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun SwitchItem(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    ListItem(
        colors = transparentListItem(),
        modifier = Modifier.selectable(
            selected = checked,
            enabled = enabled,
            role = Role.Switch,
            onClick = { onChange(!checked) },
        ),
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null, enabled = enabled) },
    )
}

@Composable
private fun AvatarButton(path: String?, name: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.clickable(onClickLabel = stringResource(R.string.action_choose_photo), onClick = onClick)) {
            Avatar(path = path, name = name, size = 80.dp)
        }
        Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun AccentSwatch(accent: AccentColor, selected: Boolean, onClick: () -> Unit) {
    val label = stringResource(
        when (accent) {
            AccentColor.PINK -> R.string.accent_pink
            AccentColor.RED -> R.string.accent_red
            AccentColor.PURPLE -> R.string.accent_purple
            AccentColor.BLUE -> R.string.accent_blue
            AccentColor.DYNAMIC -> R.string.accent_dynamic
        },
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(accentPreview(accent), CircleShape)
                .border(
                    if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else BorderStroke(0.dp, Color.Transparent),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                selected -> Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                accent == AccentColor.DYNAMIC -> Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White)
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun NamesDialog(s: CoupleSettings, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var my by rememberSaveable { mutableStateOf(s.myName) }
    var partner by rememberSaveable { mutableStateOf(s.partnerName) }
    val valid = CoupleSettings.isValidName(my) && CoupleSettings.isValidName(partner)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_names)) },
        text = {
            Column {
                NameField(my, { my = it }, stringResource(R.string.field_my_name), "my_name", ImeAction.Next)
                NameField(partner, { partner = it }, stringResource(R.string.field_partner_name), "partner_name", ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onSave(my, partner) }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(hour: Int, minute: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_reminder_time)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun transparentListItem(): ListItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)
