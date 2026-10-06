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
// These six composables are the spine of the app: every one of the 16 screens renders
// through them, so refining them here is what actually moves the whole product rather than
// one screen at a time. Signatures are intentionally unchanged so the ~900 existing call
// sites keep working.

@Composable
fun MedicalCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderStroke: BorderStroke? = BorderStroke(1.dp, MedicalBorder),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = tween(140),
        label = "cardScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 14.dp else 5.dp,
        animationSpec = tween(140),
        label = "cardElevation"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = PrimaryGreen.copy(alpha = 0.1f)),
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(MedAICorners.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = borderStroke,
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
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = tween(120),
        label = "btnScale"
    )
    val shape = RoundedCornerShape(MedAICorners.control)
    val brush = if (enabled) {
        Brush.horizontalGradient(colors = listOf(Teal500, Teal700))
    } else {
        Brush.horizontalGradient(colors = listOf(MedicalBorder, MedicalBorder))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = shape,
                ambientColor = PrimaryGreen.copy(alpha = 0.35f),
                spotColor = PrimaryGreen.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(brush)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.18f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) Color.White else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) Color.White else TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.1.sp
            )
        }
    }
}

@Composable
fun MedicalSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = tween(120),
        label = "secondaryBtnScale"
    )
    val shape = RoundedCornerShape(MedAICorners.control)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
            .clip(shape)
            .background(LightGreen)
            .border(1.5.dp, PrimaryGreen.copy(alpha = 0.45f), shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = PrimaryGreen.copy(alpha = 0.12f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = PrimaryGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MedicalDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = tween(120),
        label = "dangerBtnScale"
    )
    val shape = RoundedCornerShape(MedAICorners.control)
    val brush = if (enabled) {
        DangerGradient
    } else {
        Brush.horizontalGradient(colors = listOf(MedicalBorder, MedicalBorder))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = shape,
                ambientColor = ErrorRed.copy(alpha = 0.35f),
                spotColor = ErrorRed.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(brush)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.18f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) Color.White else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) Color.White else TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

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
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (errorText != null) ErrorRed else TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )

        var isFocused by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.55f), fontSize = 15.sp) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = when {
                        errorText != null -> ErrorRed
                        isFocused -> PrimaryGreen
                        else -> TextSecondary
                    },
                    modifier = Modifier.size(21.dp)
                )
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { isFocused = it.isFocused },
            shape = RoundedCornerShape(MedAICorners.control),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = MedicalBorder,
                errorBorderColor = ErrorRed,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = ErrorRed,
                modifier = Modifier.padding(top = 5.dp, start = 4.dp)
            )
        }
    }
}

/**
 * Top bar for every secondary screen.
 *
 * A branded gradient band rather than a white settings-style bar: it separates the page from
 * the content, carries the product's colour into every screen, and gives the back button a
 * real affordance. The action slot sits on a white pill so call sites that pass an icon with
 * an explicit dark tint (e.g. the clear-chat button on the AI doctor screen) stay legible.
 */
@Composable
fun AppHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandGradient)
            // MaterialTheme is edge-to-edge (MainActivity calls enableEdgeToEdge), and every
            // call site puts this in a Scaffold topBar slot, which does not inset its content.
            // The previous CenterAlignedTopAppBar did this for us via TopAppBarDefaults; a plain
            // Row does not, so without this the back button would sit under the status bar.
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = Color.White,
            letterSpacing = (-0.3).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (actions != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(MedAICorners.pill))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
            }
        }
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
    MedicalCard(
        modifier = modifier.padding(4.dp),
        borderStroke = BorderStroke(1.dp, DividerSoft)
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
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- SCREEN 1: SPLASH SCREEN ---

@Composable
fun HeartbeatLineAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val path = Path()

        // Create heartbeat shape
        val points = listOf(
            0.0f to 0.5f,
            0.3f to 0.5f,
            0.35f to 0.4f,
            0.4f to 0.65f,
            0.45f to 0.15f,
            0.5f to 0.85f,
            0.55f to 0.45f,
            0.6f to 0.5f,
            1.0f to 0.5f
        )

        path.moveTo(0f, height * 0.5f)
        for (i in 1 until points.size) {
            val toX = points[i].first * width
            val toY = points[i].second * height
            path.lineTo(toX, toY)
        }

        // Animated draw effect
        drawPath(
            path = path,
            color = Color(0xFF80CBC4),
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(width, width),
                    phase = (1f - progress) * width
                )
            )
        )
    }
}

@Composable
fun LoadingDotsAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotCount = 3
    val dots = List(dotCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0f at (index * 150) with FastOutSlowInEasing
                    1f at (index * 150 + 300) with FastOutSlowInEasing
                    0f at (index * 150 + 600) with FastOutSlowInEasing
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "dot_$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { anim ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .graphicsLayer {
                        scaleX = 0.5f + anim.value * 0.5f
                        scaleY = 0.5f + anim.value * 0.5f
                        alpha = 0.3f + anim.value * 0.7f
                    }
                    .background(Color(0xFF80CBC4), CircleShape)
            )
        }
    }
}

@Composable
fun SplashScreen(onNavigateToOnboarding: () -> Unit, onNavigateToHome: () -> Unit, viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(2000)
        if (user != null) {
            onNavigateToHome()
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandGradientWide),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            // Animated heartbeat line container with a beautiful medical icon inside
            Box(
                modifier = Modifier
                    .size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Heartbeat background line drawing itself
                HeartbeatLineAnimation(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                )

                // White circle with custom MedAI logo inside
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            ambientColor = Color.White,
                            spotColor = Color.White,
                        )
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_medai_logo),
                        contentDescription = "MedAI Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "MedAI",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your AI Health Assistant",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.82f)
            )

            Spacer(modifier = Modifier.height(64.dp))
            LoadingDotsAnimation()
        }
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
    val lang by viewModel.currentLanguage.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    var passwordVisible by remember { mutableStateOf(false) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalBackground)
    ) {
        // Curved top header (30% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(BrandGradientWide),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_medai_logo),
                            contentDescription = "MedAI Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "MedAI", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Your Professional AI Health Assistant", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        // Sliding card containing all forms (scrollable)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(text = Translations.getString("register", lang), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = Translations.getString("app_slogan", lang), fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    MedicalTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = Translations.getString("name_placeholder", lang),
                        leadingIcon = Icons.Default.Person,
                        placeholder = "Masalan: Asadbek Istamov"
                    )
                }

                item {
                    MedicalTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = Translations.getString("email_placeholder", lang),
                        leadingIcon = Icons.Default.Email,
                        placeholder = "example@gmail.com"
                    )
                }

                item {
                    MedicalTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = Translations.getString("phone_placeholder", lang),
                        leadingIcon = Icons.Default.Phone,
                        placeholder = "+998901234567",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }

                item {
                    MedicalTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = Translations.getString("password_placeholder", lang),
                        leadingIcon = Icons.Default.Lock,
                        placeholder = "••••••••",
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = PrimaryGreen
                                )
                            }
                        }
                    )
                }

                item {
                    MedicalTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = Translations.getString("confirm_password_placeholder", lang),
                        leadingIcon = Icons.Default.Lock,
                        placeholder = "••••••••",
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MedicalTextField(
                                value = height,
                                onValueChange = { height = it },
                                label = Translations.getString("height_placeholder", lang),
                                leadingIcon = Icons.Default.Height,
                                placeholder = "175",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MedicalTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                label = Translations.getString("weight_placeholder", lang),
                                leadingIcon = Icons.Default.Scale,
                                placeholder = "70",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Translations.getString("gender_label", lang).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = gender == "male",
                                    onClick = { gender = "male" },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                )
                                Text(Translations.getString("gender_male", lang), color = TextPrimary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = gender == "female",
                                    onClick = { gender = "female" },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                )
                                Text(Translations.getString("gender_female", lang), color = TextPrimary)
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Translations.getString("blood_type_label", lang).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            listOf("O+", "A+", "B+", "AB+", "O-", "A-").forEach { bType ->
                                FilterChip(
                                    selected = bloodType == bType,
                                    onClick = { bloodType = bType },
                                    label = { Text(bType) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    registerError?.let { message ->
                        MedAIInfoBanner(text = message, tone = MedAITone.Error)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    MedicalButton(
                        text = Translations.getString("register", lang),
                        onClick = {
                            registerError = null
                            // Check the two password fields agree before hitting the ViewModel:
                            // silently ignoring confirm_password made it decorative.
                            if (password != confirmPassword) {
                                registerError = Translations.getString("register_error_weak_password", lang)
                                return@MedicalButton
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
                        }
                    )
                }

                item {
                    // Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(MedicalBorder))
                        Text(text = " yoki ", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp))
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(MedicalBorder))
                    }
                }

                item {
                    // Google Sign-In white card
                    OutlinedButton(
                        onClick = {
                            if (!isGoogleSigningIn) {
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
                        },
                        enabled = !isGoogleSigningIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, MedicalBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        if (isGoogleSigningIn) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryGreen)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = Translations.getString("google_sign_in", lang), color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    TextButton(onClick = onNavigateToLogin) {
                        Text(text = Translations.getString("already_have_account", lang), color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// --- SCREEN 4: LOGIN SCREEN ---

@Composable
fun LoginScreen(onNavigateToRegister: () -> Unit, onLoginSuccess: () -> Unit, viewModel: AppViewModel) {
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
                        .background(Color.White, CircleShape),
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
