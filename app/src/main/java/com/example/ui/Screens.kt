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
        label = "scale"
    )
    
    val modifierWithClick = if (onClick != null) {
        modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = PrimaryGreen.copy(alpha = 0.1f)),
                onClick = onClick
            )
    } else {
        modifier
    }

    Card(
        modifier = modifierWithClick.shadow(
            elevation = if (isPressed && onClick != null) 12.dp else 4.dp,
            shape = RoundedCornerShape(20.dp),
            ambientColor = PrimaryGreen.copy(alpha = 0.12f),
            spotColor = PrimaryGreen.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
    val brush = if (enabled) {
        Brush.horizontalGradient(colors = listOf(PrimaryGreen, DarkGreen))
    } else {
        Brush.horizontalGradient(colors = listOf(Color.LightGray, Color.Gray))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = PrimaryGreen.copy(alpha = 0.35f),
                spotColor = PrimaryGreen.copy(alpha = 0.35f)
            )
            .background(brush, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(LightGreen, RoundedCornerShape(14.dp))
            .border(1.5.dp, PrimaryGreen, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
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
    val brush = if (enabled) {
        Brush.horizontalGradient(colors = listOf(ErrorRed, Color(0xFFC62828)))
    } else {
        Brush.horizontalGradient(colors = listOf(Color.LightGray, Color.Gray))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = ErrorRed.copy(alpha = 0.35f),
                spotColor = ErrorRed.copy(alpha = 0.35f)
            )
            .background(brush, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
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
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (errorText != null) ErrorRed else PrimaryGreen,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
        
        var isFocused by remember { mutableStateOf(false) }
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.6f), fontSize = 15.sp) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (errorText != null) ErrorRed else if (isFocused) PrimaryGreen else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .onFocusChanged { isFocused = it.isFocused },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF8FFFE),
                unfocusedContainerColor = Color(0xFFF8FFFE),
                errorContainerColor = Color(0xFFFFF8F8),
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
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
            )
        }
    }
}

@Composable
fun AppHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryGreen
                    )
                }
            }
        },
        actions = {
            if (actions != null) {
                actions()
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
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
        borderStroke = BorderStroke(1.dp, Color(0xFFE8F5F3))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.2.sp)
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryGreen, DarkGreen)
                )
            ),
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
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = -0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your AI Health Assistant",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF80CBC4)
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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    slides.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (currentSlide == index) 16.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (currentSlide == index) PrimaryGreen else Color.LightGray.copy(alpha = 0.5f))
                        )
                    }
                }

                MedicalButton(
                    text = if (currentSlide == 2) Translations.getString("get_started", lang) else Translations.getString("next", lang),
                    onClick = {
                        if (currentSlide < 2) {
                            currentSlide++
                        } else {
                            viewModel.setOnboardingCompleted(true)
                            onNavigateToLogin()
                        }
                    }
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
            // Top Illustration Area (45% height) with soft light green background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.48f)
                    .background(LightGreen, RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Skip Button in top right
                if (currentSlide < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.setOnboardingCompleted(true)
                                onNavigateToLogin()
                            }
                        ) {
                            Text(
                                text = Translations.getString("skip", lang),
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Language Switcher on slide 0
                if (currentSlide == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Column(modifier = Modifier.padding(start = 24.dp)) {
                            Text(
                                text = Translations.getString("select_language", lang).uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("uz" to "🇺🇿", "ru" to "🇷🇺", "en" to "🇬🇧").forEach { (code, flag) ->
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (lang == code) PrimaryGreen else MaterialTheme.colorScheme.surface)
                                            .clickable { viewModel.setLanguage(code) }
                                            .border(1.dp, if (lang == code) Color.Transparent else MedicalBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = flag, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Medical Illustration Vector (Pulse/Icon)
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .shadow(8.dp, CircleShape, ambientColor = PrimaryGreen.copy(alpha = 0.1f), spotColor = PrimaryGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = slides[currentSlide].third,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(90.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Slide Title & Subtitle with medical typography
            Text(
                text = slides[currentSlide].first,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                letterSpacing = -0.3.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = slides[currentSlide].second,
                fontSize = 15.sp,
                color = TextSecondary,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

// --- SCREEN 3: REGISTRATION SCREEN ---

@Composable
fun RegisterScreen(onNavigateToLogin: () -> Unit, onRegisterSuccess: () -> Unit, viewModel: AppViewModel) {
    val lang by viewModel.currentLanguage.collectAsState()

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
    var showGoogleChooser by remember { mutableStateOf(false) }

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
                .background(PrimaryGreen),
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
                    MedicalButton(
                        text = Translations.getString("register", lang),
                        onClick = {
                            if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty()) {
                                viewModel.registerUser(
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    dob = dob,
                                    gender = gender,
                                    bloodType = bloodType,
                                    height = height.toDoubleOrNull() ?: 175.0,
                                    weight = weight.toDoubleOrNull() ?: 70.0,
                                    onSuccess = { onRegisterSuccess() }
                                )
                            }
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
                        onClick = { showGoogleChooser = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, MedicalBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
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

                item {
                    TextButton(onClick = onNavigateToLogin) {
                        Text(text = Translations.getString("already_have_account", lang), color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showGoogleChooser) {
        GoogleAccountsSelectorDialog(
            onDismiss = { showGoogleChooser = false },
            onAccountSelected = { name, email ->
                showGoogleChooser = false
                viewModel.loginWithGoogle(name, email, onSuccess = { onRegisterSuccess() })
            },
            lang = lang
        )
    }
}

// --- SCREEN 4: LOGIN SCREEN ---

@Composable
fun LoginScreen(onNavigateToRegister: () -> Unit, onLoginSuccess: () -> Unit, viewModel: AppViewModel) {
    val lang by viewModel.currentLanguage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showGoogleChooser by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MedicalBackground)
    ) {
        // Curved top header (32% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(PrimaryGreen),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
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
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "MedAI", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = -0.5.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Your Trusted Medical AI Companion", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        // Sliding white card from bottom
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = Translations.getString("login", lang), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "MedAI - Your Clinical Co-Pilot", fontSize = 13.sp, color = TextSecondary)
                
                Spacer(modifier = Modifier.height(24.dp))

                MedicalTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = Translations.getString("email_placeholder", lang),
                    leadingIcon = Icons.Default.MedicalServices,
                    placeholder = "doctor@gmail.com"
                )

                Spacer(modifier = Modifier.height(16.dp))

                MedicalTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = Translations.getString("password_placeholder", lang),
                    leadingIcon = Icons.Default.Lock,
                    placeholder = "••••••••",
                    visualTransformation = PasswordVisualTransformation()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { /* Simulated password reset */ }) {
                        Text(text = Translations.getString("forgot_password", lang), color = TextSecondary, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                MedicalButton(
                    text = Translations.getString("login", lang),
                    onClick = {
                        if (email.isNotEmpty()) {
                            viewModel.loginUser(email, onSuccess = { onLoginSuccess() })
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MedicalBorder))
                    Text(text = " yoki ", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp))
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MedicalBorder))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign-In Button with border and light theme colors
                OutlinedButton(
                    onClick = { showGoogleChooser = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, MedicalBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
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

                Spacer(modifier = Modifier.weight(1.0f))

                TextButton(onClick = onNavigateToRegister) {
                    Text(text = Translations.getString("no_account_yet", lang), color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showGoogleChooser) {
        GoogleAccountsSelectorDialog(
            onDismiss = { showGoogleChooser = false },
            onAccountSelected = { name, email ->
                showGoogleChooser = false
                viewModel.loginWithGoogle(name, email, onSuccess = { onLoginSuccess() })
            },
            lang = lang
        )
    }
}

@Composable
fun GoogleAccountsSelectorDialog(
    onDismiss: () -> Unit,
    onAccountSelected: (name: String, email: String) -> Unit,
    lang: String
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .padding(horizontal = 0.dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MedicalBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.2f))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text(text = "o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text(text = "o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text(text = "g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text(text = "l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text(text = "e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = Translations.getString("google_choose_account", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = Translations.getString("google_continue_to", lang),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val accounts = listOf(
                        Pair("Asadbek Istamov", "asadbekistamov99@gmail.com"),
                        Pair("Asadbek Health", "asadbek.health@gmail.com"),
                        Pair("Istamov Personal", "istamov.personal@gmail.com")
                    )
                    
                    accounts.forEach { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onAccountSelected(account.first, account.second)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (account.second.contains("99")) PrimaryGreen else AccentCyan
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = account.first.take(1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(
                                    text = account.first,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = account.second,
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAccountSelected("Yangi Foydalanuvchi", "new.user@gmail.com")
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LightGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = Translations.getString("google_add_account", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryGreen
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = Translations.getString("google_terms", lang),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
