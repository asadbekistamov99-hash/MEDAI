@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ripple
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.*
import com.example.i18n.Translations
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- GLOBAL MEDICAL COMPONENTS ---
//
// These composables are the spine of the app: nearly every screen renders through them, so they
// are thin wrappers over MedAIKit.kt. Signatures are unchanged so existing call sites keep
// working; colours come from MedAITheme, so light and dark both work.

private val DefaultCardBorder = BorderStroke(1.dp, MedicalBorder)

@Composable
fun MedicalCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderStroke: BorderStroke? = DefaultCardBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    val medai = MedAITheme.colors
    // The default border is a light-only constant; swap it for the themed hairline.
    val stroke = if (borderStroke === DefaultCardBorder) BorderStroke(1.dp, medai.border) else borderStroke
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.985f else 1f,
        animationSpec = tween(120),
        label = "cardScale"
    )
    val shape = RoundedCornerShape(MedAICorners.card)
    Column(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(medai.surface)
            .then(if (stroke != null) Modifier.border(stroke, shape) else Modifier)
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
                } else Modifier
            ),
        content = content
    )
}

@Composable
fun MedicalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) = MedAIPrimaryButton(text = text, onClick = onClick, modifier = modifier.fillMaxWidth(), icon = icon, enabled = enabled)

@Composable
fun MedicalSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) = MedAISecondaryButton(text = text, onClick = onClick, modifier = modifier.fillMaxWidth(), icon = icon, enabled = enabled)

@Composable
fun MedicalDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) = MedAIDangerButton(text = text, onClick = onClick, modifier = modifier.fillMaxWidth(), icon = icon, enabled = enabled)

@Composable
fun MedicalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    placeholder: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    errorText: String? = null
) = MedAITextField(
    value = value,
    onValueChange = onValueChange,
    label = label,
    modifier = Modifier.fillMaxWidth(),
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    error = errorText,
    visualTransformation = visualTransformation,
    trailingContent = trailingIcon,
    keyboardOptions = keyboardOptions,
)

/**
 * Top bar for every secondary screen: flat, on the canvas, with a hairline underneath.
 *
 * MaterialTheme is edge-to-edge (MainActivity calls enableEdgeToEdge) and call sites put this in
 * a Scaffold topBar slot, which does not inset its content, hence the explicit status-bar padding.
 */
@Composable
fun AppHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val medai = MedAITheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(medai.canvas)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(MinTouch)
                        .clip(CircleShape)
                        .clickable(role = Role.Button) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = medai.textPrimary
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = medai.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            if (actions != null) {
                Row(verticalAlignment = Alignment.CenterVertically) { actions() }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(medai.border))
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val medai = MedAITheme.colors

    MedicalCard(
        modifier = modifier.padding(4.dp),
        borderStroke = BorderStroke(1.dp, medai.divider)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(23.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = medai.textSecondary
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = medai.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- SCREEN 1: SPLASH SCREEN ---

@Composable
fun SplashScreen(onNavigateToOnboarding: () -> Unit, onNavigateToHome: () -> Unit, viewModel: AppViewModel) {
    val c = MedAITheme.colors

    val user by viewModel.currentUser.collectAsState()
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // One short entrance (no endless loops): logo settles in, text fades up.
        launch { alpha.animateTo(1f, tween(450)) }
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
        delay(1200)
        if (user != null) {
            onNavigateToHome()
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.heroBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { this.alpha = alpha.value; scaleX = scale.value; scaleY = scale.value }
        ) {
            Box(
                modifier = Modifier.size(104.dp).background(c.surface, CircleShape).padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_medai_logo),
                    contentDescription = "MedAI",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "MedAI", style = MaterialTheme.typography.displayMedium, color = c.onHero)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = Translations.getString("app_slogan", viewModel.currentLanguage.collectAsState().value),
                style = MaterialTheme.typography.bodyLarge,
                color = c.onHeroMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        MedAISpinner(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 48.dp), size = 24.dp)
    }
}

// --- SCREEN 2: ONBOARDING ---

@Composable
fun OnboardingScreen(onNavigateToLogin: () -> Unit, viewModel: AppViewModel) {
    var currentSlide by remember { mutableStateOf(0) }
    val lang by viewModel.currentLanguage.collectAsState()
    val c = MedAITheme.colors

    val slides = listOf(
        Triple(
            Translations.getString("onboarding_title_1", lang),
            Translations.getString("onboarding_desc_1", lang),
            Icons.Default.HealthAndSafety
        ),
        Triple(
            Translations.getString("onboarding_title_2", lang),
            Translations.getString("onboarding_desc_2", lang),
            Icons.Default.People
        ),
        Triple(
            Translations.getString("onboarding_title_3", lang),
            Translations.getString("onboarding_desc_3", lang),
            Icons.Default.Star
        )
    )

    Scaffold(
        containerColor = c.canvas,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress dots: the active one stretches into a pill.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    slides.forEachIndexed { index, _ ->
                        val width by animateDpAsState(
                            targetValue = if (currentSlide == index) 24.dp else 8.dp,
                            animationSpec = tween(200),
                            label = "onboardingDot"
                        )
                        Box(
                            modifier = Modifier
                                .size(width, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (currentSlide == index) c.brand else c.borderStrong.copy(alpha = 0.5f))
                        )
                    }
                }

                MedAIPrimaryButton(
                    text = if (currentSlide == 2) Translations.getString("get_started", lang) else Translations.getString("next", lang),
                    onClick = {
                        if (currentSlide < 2) {
                            currentSlide++
                        } else {
                            viewModel.setOnboardingCompleted(true)
                            onNavigateToLogin()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top illustration area with a soft brand tint.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.48f)
                    .background(c.brandSoft, RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                // Skip button in the top right (48dp hit area).
                if (currentSlide < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp, end = 8.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        MedAITextButton(
                            text = Translations.getString("skip", lang),
                            onClick = {
                                viewModel.setOnboardingCompleted(true)
                                onNavigateToLogin()
                            },
                        )
                    }
                }

                // Language switcher on slide 0
                if (currentSlide == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Column(modifier = Modifier.padding(start = 24.dp, top = 4.dp)) {
                            Text(
                                text = Translations.getString("select_language", lang).uppercase(),
                                style = MedAIText.Eyebrow,
                                color = c.onBrandSoft
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                listOf("uz" to "🇺🇿", "ru" to "🇷🇺", "en" to "🇬🇧").forEach { (code, flag) ->
                                    // 48dp hit area around a 40dp circle.
                                    Box(
                                        modifier = Modifier
                                            .size(MinTouch)
                                            .clip(CircleShape)
                                            .clickable(role = Role.RadioButton) { viewModel.setLanguage(code) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (lang == code) c.brand else c.surface)
                                                .border(1.dp, if (lang == code) Color.Transparent else c.borderStrong, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = flag, fontSize = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Illustration disc
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(c.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = currentSlide, animationSpec = tween(220), label = "onboardingIcon") { slide ->
                        Icon(
                            imageVector = slides[slide].third,
                            contentDescription = null,
                            tint = c.brand,
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Crossfade(
                targetState = currentSlide,
                animationSpec = tween(220),
                label = "onboardingText",
                modifier = Modifier.weight(1f)
            ) { slide ->
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = slides[slide].first,
                        style = MaterialTheme.typography.displaySmall,
                        color = c.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = slides[slide].second,
                        style = MaterialTheme.typography.bodyLarge,
                        color = c.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}

// --- SCREEN 3: REGISTRATION SCREEN ---

@Composable
fun RegisterScreen(onNavigateToLogin: () -> Unit, onRegisterSuccess: () -> Unit, viewModel: AppViewModel) {
    val c = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun t(key: String) = Translations.getString(key, lang)

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("1999-05-15") }
    var gender by remember { mutableStateOf("male") }
    var bloodType by remember { mutableStateOf("O+") }
    var height by remember { mutableStateOf("175") }
    var weight by remember { mutableStateOf("70") }

    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.surface)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        // Compact brand header: the form is long, so it gets less hero than Login.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.heroBrush)
                .statusBarsPadding()
                .padding(top = 16.dp, bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).background(c.surface, CircleShape).padding(2.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.img_medai_logo),
                        contentDescription = "MedAI",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text("MedAI", style = MaterialTheme.typography.headlineLarge, color = c.onHero)
            }
        }

        Column(
            modifier = Modifier
                .offset(y = (-28).dp)
                .fillMaxWidth()
                .background(c.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(t("register"), style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
                Text(t("app_slogan"), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            }

            MedAITextField(
                value = name, onValueChange = { name = it }, label = t("name_placeholder"),
                modifier = Modifier.fillMaxWidth(), leadingIcon = Icons.Default.Person, placeholder = "Asadbek Istamov"
            )
            MedAITextField(
                value = email, onValueChange = { email = it }, label = t("email_placeholder"),
                modifier = Modifier.fillMaxWidth(), leadingIcon = Icons.Default.Email, placeholder = "example@gmail.com",
                keyboardType = KeyboardType.Email
            )
            MedAITextField(
                value = phone, onValueChange = { phone = it }, label = t("phone_placeholder"),
                modifier = Modifier.fillMaxWidth(), leadingIcon = Icons.Default.Phone, placeholder = "+998901234567",
                keyboardType = KeyboardType.Phone
            )
            MedAITextField(
                value = password, onValueChange = { password = it }, label = t("password_placeholder"),
                modifier = Modifier.fillMaxWidth(), leadingIcon = Icons.Default.Lock, isPassword = true,
                showPasswordDescription = t("show_password"), hidePasswordDescription = t("hide_password")
            )
            MedAITextField(
                value = confirmPassword, onValueChange = { confirmPassword = it }, label = t("confirm_password_placeholder"),
                modifier = Modifier.fillMaxWidth(), leadingIcon = Icons.Default.Lock, isPassword = true,
                showPasswordDescription = t("show_password"), hidePasswordDescription = t("hide_password")
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MedAITextField(
                    value = height, onValueChange = { height = it }, label = t("height_placeholder"),
                    modifier = Modifier.weight(1f), leadingIcon = Icons.Default.Height, placeholder = "175",
                    keyboardType = KeyboardType.Number
                )
                MedAITextField(
                    value = weight, onValueChange = { weight = it }, label = t("weight_placeholder"),
                    modifier = Modifier.weight(1f), leadingIcon = Icons.Default.Scale, placeholder = "70",
                    keyboardType = KeyboardType.Number
                )
            }

            Column {
                Text(t("gender_label"), style = MaterialTheme.typography.labelLarge, color = c.textPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedAIFilterChip(t("gender_male"), gender == "male", { gender = "male" })
                    MedAIFilterChip(t("gender_female"), gender == "female", { gender = "female" })
                }
            }

            Column {
                Text(t("blood_type_label"), style = MaterialTheme.typography.labelLarge, color = c.textPrimary)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("O+", "A+", "B+", "AB+", "O-", "A-").forEach { bType ->
                        MedAIFilterChip(bType, bloodType == bType, { bloodType = bType })
                    }
                }
            }

            registerError?.let { message -> MedAIInfoBanner(text = message, tone = MedAITone.Error) }

            MedAIPrimaryButton(
                text = t("register"),
                onClick = {
                    registerError = null
                    // Check the two password fields agree before hitting the ViewModel:
                    // silently ignoring confirm_password made it decorative.
                    if (password != confirmPassword) {
                        registerError = t("register_error_weak_password")
                        return@MedAIPrimaryButton
                    }
                    viewModel.registerUser(
                        name = name,
                        email = email,
                        phone = phone,
                        dob = dob,
                        gender = gender,
                        bloodType = bloodType,
                        height = height.toDoubleOrNull() ?: 175.0,
                        weight = weight.toDoubleOrNull() ?: 70.0,
                        password = password,
                        onSuccess = { onRegisterSuccess() },
                        onError = { registerError = it }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(1.dp).background(c.border))
                Text(t("or_divider"), style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(horizontal = 12.dp))
                Box(Modifier.weight(1f).height(1.dp).background(c.border))
            }

            val googleShape = RoundedCornerShape(MedAICorners.control)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(googleShape)
                    .background(c.surface)
                    .border(1.5.dp, c.borderStrong, googleShape)
                    .clickable(enabled = !isGoogleSigningIn, role = Role.Button) {
                        isGoogleSigningIn = true
                        scope.launch {
                            val result = com.example.auth.GoogleAuthHelper.signIn(context)
                            isGoogleSigningIn = false
                            result.onSuccess { account ->
                                viewModel.loginWithGoogle(account.name, account.email, onSuccess = { onRegisterSuccess() })
                            }.onFailure { e ->
                                Toast.makeText(context, e.localizedMessage ?: "Google orqali ro'yxatdan o'tishda xatolik", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isGoogleSigningIn) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = c.brand)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            t("google_sign_in"), style = MaterialTheme.typography.labelLarge, fontSize = 15.sp,
                            color = c.textPrimary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }

            MedAITextButton(text = t("already_have_account"), onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
        }
    }
}

// --- SCREEN 4: LOGIN SCREEN ---

@Composable
fun LoginScreen(onNavigateToRegister: () -> Unit, onLoginSuccess: () -> Unit, viewModel: AppViewModel) {
    val medai = MedAITheme.colors

    val lang by viewModel.currentLanguage.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val c = MedAITheme.colors
    fun t(key: String) = Translations.getString(key, lang)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.surface)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        // Brand header. White text on the gradient is >= 5.4:1 at both ends.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.heroBrush)
                .statusBarsPadding()
                .padding(top = 24.dp, bottom = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(medai.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_medai_logo),
                        contentDescription = "MedAI",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "MedAI", style = MaterialTheme.typography.displaySmall, color = c.onHero)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = t("app_slogan"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.onHeroMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

        // Form sheet, overlapping the header with rounded top corners.
        Column(
            modifier = Modifier
                .offset(y = (-28).dp)
                .fillMaxWidth()
                .background(c.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 8.dp)
        ) {
            Text(text = t("login"), style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)

            Spacer(modifier = Modifier.height(20.dp))

            MedAITextField(
                value = email,
                onValueChange = { email = it },
                label = t("email_placeholder"),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Email,
                placeholder = "doctor@gmail.com",
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(16.dp))

            MedAITextField(
                value = password,
                onValueChange = { password = it },
                label = t("password_placeholder"),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                showPasswordDescription = t("show_password"),
                hidePasswordDescription = t("hide_password"),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                MedAITextButton(
                    text = t("forgot_password"),
                    onClick = { /* Simulated password reset */ },
                    color = c.textSecondary,
                )
            }

            // Show why the login was refused instead of silently doing nothing.
            authError?.let { message ->
                MedAIInfoBanner(text = message, tone = MedAITone.Error)
                Spacer(modifier = Modifier.height(12.dp))
            }

            MedAIPrimaryButton(
                text = t("login"),
                loading = isSigningIn,
                onClick = {
                    authError = null
                    isSigningIn = true
                    viewModel.loginUser(
                        email = email,
                        password = password,
                        onSuccess = { onLoginSuccess() },
                        onError = {
                            isSigningIn = false
                            authError = it
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(c.border))
                Text(
                    text = t("or_divider"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(c.border))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign-In
            val googleShape = RoundedCornerShape(MedAICorners.control)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(googleShape)
                    .background(c.surface)
                    .border(1.5.dp, c.borderStrong, googleShape)
                    .clickable(enabled = !isGoogleSigningIn, role = Role.Button) {
                        isGoogleSigningIn = true
                        scope.launch {
                            val result = com.example.auth.GoogleAuthHelper.signIn(context)
                            isGoogleSigningIn = false
                            result.onSuccess { account ->
                                viewModel.loginWithGoogle(account.name, account.email, onSuccess = { onLoginSuccess() })
                            }.onFailure { e ->
                                Toast.makeText(context, e.localizedMessage ?: "Google orqali kirishda xatolik", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isGoogleSigningIn) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = c.brand)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(text = "G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = t("google_sign_in"),
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 15.sp,
                            color = c.textPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            MedAITextButton(
                text = t("no_account_yet"),
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
