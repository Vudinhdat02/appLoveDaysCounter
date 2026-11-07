package com.example.lovedays.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lovedays.R
import com.example.lovedays.data.datastore.CoupleSettings
import com.example.lovedays.ui.components.Avatar
import com.example.lovedays.ui.components.BeatingHeart
import com.example.lovedays.ui.components.LoveDatePickerDialog
import com.example.lovedays.ui.components.MilestoneText
import java.time.LocalDate

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.step != OnboardingStep.WELCOME) { viewModel.back() }

    val pickMine = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::pickMyAvatar)
    }
    val pickPartner = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::pickPartnerAvatar)
    }
    // Từ chối quyền thì app vẫn chạy bình thường: luôn hoàn tất onboarding.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.complete()
    }
    val imageRequest = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.step != OnboardingStep.WELCOME) {
                LinearProgressIndicator(
                    progress = { state.step.ordinal / (OnboardingStep.entries.size - 1f) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
            }
            AnimatedContent(
                targetState = state.step,
                label = "onboarding",
                modifier = Modifier.weight(1f).widthIn(max = 560.dp),
            ) { step ->
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStep(onStart = viewModel::next)
                    OnboardingStep.NAMES -> NamesStep(
                        myName = state.myName,
                        partnerName = state.partnerName,
                        onMyNameChange = viewModel::setMyName,
                        onPartnerNameChange = viewModel::setPartnerName,
                        onBack = viewModel::back,
                        onContinue = viewModel::next,
                    )
                    OnboardingStep.START_DATE -> StartDateStep(
                        startDate = state.startDate,
                        today = viewModel.today,
                        onDateSelected = viewModel::setStartDate,
                        onBack = viewModel::back,
                        onContinue = viewModel::next,
                    )
                    OnboardingStep.AVATARS -> AvatarsStep(
                        state = state,
                        onPickMine = { pickMine.launch(imageRequest) },
                        onPickPartner = { pickPartner.launch(imageRequest) },
                        onBack = viewModel::back,
                        onContinue = viewModel::next,
                    )
                    OnboardingStep.NOTIFICATIONS -> NotificationStep(
                        onAllow = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.complete()
                            }
                        },
                        onSkip = viewModel::complete,
                        onBack = viewModel::back,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepLayout(
    title: String,
    subtitle: String?,
    primaryLabel: String,
    primaryEnabled: Boolean,
    onPrimary: () -> Unit,
    onBack: (() -> Unit)?,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(Modifier.height(32.dp))
            content()
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            }
            Spacer(Modifier.weight(1f))
            if (secondaryLabel != null && onSecondary != null) {
                OutlinedButton(onClick = onSecondary, modifier = Modifier.testTag("secondary")) { Text(secondaryLabel) }
            }
            Button(
                onClick = onPrimary,
                enabled = primaryEnabled,
                modifier = Modifier.testTag("continue"),
            ) { Text(primaryLabel) }
        }
    }
}

@Composable
fun WelcomeStep(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BeatingHeart(size = 120.dp)
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.onboarding_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        Spacer(Modifier.height(40.dp))
        Button(onClick = onStart, modifier = Modifier.testTag("continue")) {
            Text(stringResource(R.string.onboarding_start))
        }
    }
}

@Composable
fun NamesStep(
    myName: String,
    partnerName: String,
    onMyNameChange: (String) -> Unit,
    onPartnerNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val valid = CoupleSettings.isValidName(myName) && CoupleSettings.isValidName(partnerName)
    StepLayout(
        title = stringResource(R.string.onboarding_names_title),
        subtitle = stringResource(R.string.onboarding_names_subtitle),
        primaryLabel = stringResource(R.string.action_continue),
        primaryEnabled = valid,
        onPrimary = onContinue,
        onBack = onBack,
    ) {
        NameField(myName, onMyNameChange, stringResource(R.string.field_my_name), "my_name", ImeAction.Next)
        Spacer(Modifier.height(16.dp))
        NameField(partnerName, onPartnerNameChange, stringResource(R.string.field_partner_name), "partner_name", ImeAction.Done)
    }
}

@Composable
fun NameField(value: String, onChange: (String) -> Unit, label: String, tag: String, imeAction: ImeAction) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.take(CoupleSettings.NAME_MAX_LENGTH)) },
        label = { Text(label) },
        singleLine = true,
        supportingText = { Text("${value.length}/${CoupleSettings.NAME_MAX_LENGTH}") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = imeAction),
        modifier = Modifier.fillMaxWidth().testTag(tag),
    )
}

@Composable
fun StartDateStep(
    startDate: LocalDate?,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    StepLayout(
        title = stringResource(R.string.onboarding_date_title),
        subtitle = stringResource(R.string.onboarding_date_subtitle),
        primaryLabel = stringResource(R.string.action_continue),
        primaryEnabled = startDate != null,
        onPrimary = onContinue,
        onBack = onBack,
    ) {
        OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth().testTag("pick_date")) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text(
                startDate?.let { MilestoneText.date(it) } ?: stringResource(R.string.onboarding_pick_date),
                fontSize = 18.sp,
            )
        }
    }
    if (showPicker) {
        LoveDatePickerDialog(
            initial = startDate,
            maxDate = today,
            onDismiss = { showPicker = false },
            onConfirm = {
                onDateSelected(it)
                showPicker = false
            },
        )
    }
}

@Composable
private fun AvatarsStep(
    state: OnboardingUiState,
    onPickMine: () -> Unit,
    onPickPartner: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val hasAny = state.myAvatarPath != null || state.partnerAvatarPath != null
    StepLayout(
        title = stringResource(R.string.onboarding_avatars_title),
        subtitle = stringResource(R.string.onboarding_avatars_subtitle),
        primaryLabel = stringResource(if (hasAny) R.string.action_continue else R.string.action_skip),
        primaryEnabled = true,
        onPrimary = onContinue,
        onBack = onBack,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarPicker(state.myAvatarPath, state.myName, onPickMine)
            BeatingHeart(size = 36.dp, modifier = Modifier.padding(horizontal = 12.dp))
            AvatarPicker(state.partnerAvatarPath, state.partnerName, onPickPartner)
        }
    }
}

@Composable
private fun AvatarPicker(path: String?, name: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.clickable(onClickLabel = stringResource(R.string.action_choose_photo), onClick = onClick)) {
            Avatar(path = path, name = name, size = 112.dp)
        }
        Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        TextButton(onClick = onClick) { Text(stringResource(R.string.action_choose_photo)) }
    }
}

@Composable
private fun NotificationStep(onAllow: () -> Unit, onSkip: () -> Unit, onBack: () -> Unit) {
    StepLayout(
        title = stringResource(R.string.onboarding_notif_title),
        subtitle = stringResource(R.string.onboarding_notif_subtitle),
        primaryLabel = stringResource(R.string.onboarding_notif_allow),
        primaryEnabled = true,
        onPrimary = onAllow,
        onBack = onBack,
        secondaryLabel = stringResource(R.string.onboarding_notif_later),
        onSecondary = onSkip,
    ) {
        Icon(
            Icons.Filled.NotificationsActive,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp).height(96.dp).widthIn(min = 96.dp),
        )
    }
}
