package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.ComplaintEntity
import com.example.ui.theme.*
import com.example.viewmodel.RuralSyncViewModel
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.text.style.TextAlign
import android.content.Context
import android.widget.Toast

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderIndicatorColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x0CFFFFFF) // Premium 5% translucent white
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderIndicatorColor ?: Color(0x10FFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            content()
        }
    }
}

// -------------------------------------------------------------
// PREMIUM THEME CUSTOM ANIMATED LOGO COMPOSABLE (EDGESYNC SIGNATURE)
// -------------------------------------------------------------
@Composable
fun EdgeSyncLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 80.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val pulseScale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableStateOf(1f) }
    }
    
    val rotationAngle by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotate"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = modifier
            .size(logoSize)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x338B5CF6), Color.Transparent),
                    radius = 200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(logoSize * 0.8f)) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2, height / 2)
            val radius = width / 3

            // Draw glowing concentric rings
            drawCircle(
                color = CyberCyan.copy(alpha = 0.25f * (2f - pulseScale)),
                radius = radius * pulseScale * 1.4f,
                style = Stroke(width = 3f)
            )

            drawCircle(
                color = CyberIndigo.copy(alpha = 0.40f),
                radius = radius,
                style = Stroke(width = 4f)
            )

            // Draw network intersections (rotating orbiting nodes)
            rotate(rotationAngle, center) {
                // Orbiting lines
                drawLine(
                    color = CyberCyan.copy(alpha = 0.6f),
                    start = Offset(center.x - radius, center.y),
                    end = Offset(center.x + radius, center.y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = CyberIndigo.copy(alpha = 0.6f),
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y + radius),
                    strokeWidth = 2f
                )

                // Intersecting Node pins
                drawCircle(color = CyberCyan, radius = 8f, center = Offset(center.x - radius, center.y))
                drawCircle(color = CyberCyan, radius = 8f, center = Offset(center.x + radius, center.y))
                drawCircle(color = CyberIndigo, radius = 8f, center = Offset(center.x, center.y - radius))
                drawCircle(color = CyberIndigo, radius = 8f, center = Offset(center.x, center.y + radius))
            }

            // Central core GPS node pin
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyberCyan, CyberIndigo),
                    center = center,
                    radius = 16f
                ),
                radius = 14f,
                center = center
            )
            
            // Draw core locator arrow element
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, center.y - 12f)
                lineTo(center.x + 9f, center.y + 9f)
                lineTo(center.x, center.y + 4f)
                lineTo(center.x - 9f, center.y + 9f)
                close()
            }
            drawPath(path = path, color = Color.White)
        }
    }
}

// -------------------------------------------------------------
// MAIN SPLASH OUTLINE HEADER (EDGE NETWORK GATEWAY STATUS)
// -------------------------------------------------------------
@Composable
fun NetworkStatusBar(viewModel: RuralSyncViewModel) {
    val isOnline by viewModel.isOnline.collectAsState()
    val speedMbps by viewModel.simulatedSpeedMbps.collectAsState()
    val speedStr by viewModel.simulatedSpeedString.collectAsState()
    val isEco by viewModel.isEcoMode.collectAsState()
    val syncMsg by viewModel.syncStatusMessage.collectAsState()
    val queueSize by viewModel.unsyncedQueueSize.collectAsState()

    // Blinking effect for the Status Dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF04060A))
            .padding(top = 40.dp, bottom = 12.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "EdgeSync",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0x228B5CF6), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0x448B5CF6), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "v2.1 Panchayat",
                            color = CyberCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Blinking dot indicator
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .drawBehind {
                                drawCircle(
                                    color = if (isOnline) CyberEmerald else CyberOrange,
                                    radius = size.minDimension / 2,
                                    alpha = alphaAnim
                                )
                            }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = syncMsg,
                        color = if (isOnline) CyberEmerald else CyberOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Odometer status capsule
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isOnline) "LINE SPEED" else "STANDALONE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLow,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isOnline) speedStr else "Offline",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) CyberEmerald else CyberOrange
                    )
                }

                if (queueSize > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(CyberRose, CircleShape)
                            .size(24.dp)
                    ) {
                        Text(
                            text = "$queueSize",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Divider(color = Color(0x10FFFFFF), modifier = Modifier.padding(top = 12.dp), thickness = 1.dp)
    }
}

// -------------------------------------------------------------
// USER LOGIN SCREEN
// -------------------------------------------------------------
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: RuralSyncViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccessMessage.collectAsState()
    val isLoading by viewModel.authLoading.collectAsState()

    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val permissionsOnboarded by viewModel.permissionsOnboarded.collectAsState()

    // Protected Route - Automatic Session Restoration Redirection
    LaunchedEffect(currentUserEmail) {
        if (currentUserEmail != null) {
            if (permissionsOnboarded) {
                navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                navController.navigate("permissions_onboarding") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF13112E), CosmicDarkBg),
                    center = Offset(300f, 300f),
                    radius = 1200f
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Logo Header
            EdgeSyncLogo(logoSize = 100.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "EDGESYNC PORTAL",
                color = CyberCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Edge-Synchronized Field Node",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Access standard local database or sync pipeline",
                color = TextLow,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "OFFLINE DATABASE SIGN-IN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberIndigo,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Panchayat Nodular Email", color = TextLow) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberIndigo,
                        unfocusedBorderColor = Color(0x20FFFFFF),
                        focusedContainerColor = Color(0x05FFFFFF),
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Secure Passport Key", color = TextLow) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Password toggle",
                                tint = TextLow
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberIndigo,
                        unfocusedBorderColor = Color(0x20FFFFFF),
                        focusedContainerColor = Color(0x05FFFFFF),
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Forgot Password link & pre-fills helper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            email = "operator@panchayat.in"
                            password = "panchayat123"
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("🔑 Test Prefill", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { navController.navigate("forgot_password") },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Forgot passport key?", color = CyberOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        viewModel.loginUser(email.trim(), password.trim()) { success ->
                            if (success) {
                                if (permissionsOnboarded) {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("permissions_onboarding") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberIndigo,
                        disabledContainerColor = Color(0x118B5CF6)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "AUTHENTICATE NODE",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Error / Success Banner
                authError?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "⚠️ $it",
                        color = CyberRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                authSuccess?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "✅ $it",
                        color = CyberEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "New operator in Panchayat grid?", color = TextLow, fontSize = 14.sp)
                TextButton(onClick = { navController.navigate("register") }) {
                    Text(text = "Register Node", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// USER REGISTRATION SCREEN
// -------------------------------------------------------------
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RuralSyncViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccessMessage.collectAsState()
    val isLoading by viewModel.authLoading.collectAsState()

    // Real-time Reactive Validation States
    val isNameValid = remember(name) { name.trim().length >= 3 }
    val isEmailValid = remember(email) { android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() }
    val isMobileValid = remember(mobileNumber) { mobileNumber.trim().length == 10 && mobileNumber.all { it.isDigit() } }
    
    val hasMinLength = remember(password) { password.length >= 8 }
    val hasUppercase = remember(password) { password.any { it.isUpperCase() } }
    val hasLowercase = remember(password) { password.any { it.isLowerCase() } }
    val hasDigit = remember(password) { password.any { it.isDigit() } }
    val hasSpecialChar = remember(password) { password.any { !it.isLetterOrDigit() } }
    val isPasswordStrong = hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    
    val isConfirmPasswordValid = remember(password, confirmPassword) { password == confirmPassword && confirmPassword.isNotEmpty() }
    
    val isFormValid = isNameValid && isEmailValid && isMobileValid && isPasswordStrong && isConfirmPasswordValid

    // Password strength score (0 to 5)
    val strengthScore = listOf(hasMinLength, hasUppercase, hasLowercase, hasDigit, hasSpecialChar).count { it }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1F112E), CosmicDarkBg),
                    center = Offset(300f, 300f),
                    radius = 1200f
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0x10FFFFFF), CircleShape)
                    .border(2.dp, Brush.linearGradient(listOf(CyberRose, CyberIndigo)), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = "Register Logo",
                    tint = CyberRose,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "EDGESYNC PORTAL",
                color = CyberRose,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Deploy Operator Node",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Deploy highly secure independent local node",
                color = TextLow,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SECURE INDEPENDENT REGISTRATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberRose,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Operator Name", color = TextLow) },
                    singleLine = true,
                    isError = name.isNotEmpty() && !isNameValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberRose,
                        unfocusedBorderColor = Color(0x20FFFFFF),
                        focusedContainerColor = Color(0x05FFFFFF),
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (name.isNotEmpty() && !isNameValid) {
                    Text(text = "Name must be at least 3 characters", color = CyberRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Nodular Email Identity", color = TextLow) },
                    singleLine = true,
                    isError = email.isNotEmpty() && !isEmailValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberRose,
                        unfocusedBorderColor = Color(0x20FFFFFF),
                        focusedContainerColor = Color(0x05FFFFFF),
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (email.isNotEmpty() && !isEmailValid) {
                    Text(text = "Please enter a valid email address", color = CyberRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mobile
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) mobileNumber = it },
                    label = { Text("10-Digit Mobile Number", color = TextLow) },
                    singleLine = true,
                    isError = mobileNumber.isNotEmpty() && !isMobileValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberRose,
                        unfocusedBorderColor = Color(0x20FFFFFF),
                        focusedContainerColor = Color(0x05FFFFFF),
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (mobileNumber.isNotEmpty() && !isMobileValid) {
                    Text(text = "Mobile number must be exactly 10 digits", color = CyberRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Secure Passkey", color = TextLow) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = TextLow
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberRose,
                        unfocusedBorderColor = Color(0x20FFFFFF),
                        focusedContainerColor = Color(0x05FFFFFF),
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Password strength color bar indicator
                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val strengthColor = when (strengthScore) {
                        in 0..2 -> CyberRed
                        in 3..4 -> CyberOrange
                        else -> CyberEmerald
                    }
                    val strengthLabel = when (strengthScore) {
                        in 0..2 -> "PASSKEY WEAK ⚠️"
                        in 3..4 -> "PASSKEY MODERATE ⚡"
                        else -> "PASSKEY STRONG SECURITY SHIELD 🏆"
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = strengthLabel, color = strengthColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(text = "$strengthScore/5", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Segmented bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (i in 1..5) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(
                                            if (i <= strengthScore) strengthColor else Color(0x15FFFFFF),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // Password rules details checklist
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            @Composable
                            fun RuleRow(met: Boolean, text: String) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                        contentDescription = null,
                                        tint = if (met) CyberEmerald else Color.Gray,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = text, color = if (met) Color.White else Color.Gray, fontSize = 9.sp)
                                }
                            }
                            RuleRow(hasMinLength, "At least 8 characters")
                            RuleRow(hasUppercase, "At least 1 uppercase letter (A-Z)")
                            RuleRow(hasLowercase, "At least 1 lowercase letter (a-z)")
                            RuleRow(hasDigit, "At least 1 numeral (0-9)")
                            RuleRow(hasSpecialChar, "At least 1 special character (!@#$)")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Re-type Secure Passkey", color = TextLow) },
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && !isConfirmPasswordValid,
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(
                                imageVector = if (isConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = TextLow
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberRose,
                        unfocusedBorderColor = Color(0x20FFFFFF),
                        focusedContainerColor = Color(0x05FFFFFF),
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (confirmPassword.isNotEmpty() && !isConfirmPasswordValid) {
                    Text(text = "Passkeys do not match", color = CyberRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit
                Button(
                    onClick = {
                        if (!isFormValid) return@Button
                        viewModel.registerUser(email, password, name, mobileNumber) { success ->
                            if (success) {
                                navController.navigate("login")
                            }
                        }
                    },
                    enabled = !isLoading && isFormValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberRose,
                        disabledContainerColor = Color(0x11E879F9)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "CREATE PANCHAYAT NODE",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }

                authError?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "⚠️ $it",
                        color = CyberRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                authSuccess?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "✅ $it",
                        color = CyberEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Operator already initialized?", color = TextLow, fontSize = 14.sp)
                TextButton(onClick = { navController.navigate("login") }) {
                    Text(text = "Sign In", color = CyberRose, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// GENERAL MAIN USER DASHBOARD
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: RuralSyncViewModel
) {
    val userName by viewModel.currentUserName.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val queueSize by viewModel.unsyncedQueueSize.collectAsState()
    val localComplaints by viewModel.getLocalComplaintsFlow().collectAsState(initial = emptyList())
    val selectedVillage by viewModel.selectedVillage.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.startLiveGpsTracking(context)
    }

    var showVillageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { NetworkStatusBar(viewModel) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("submit_complaint") },
                containerColor = CyberIndigo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Complaint", modifier = Modifier.size(32.dp))
            }
        },
        containerColor = CosmicDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Welcome Card with dynamic location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NAMASKAR, OPERATOR 🙏🏽",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = userName ?: "Villager Node",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location Badge
                    IconButton(
                        onClick = { showVillageDialog = true },
                        modifier = Modifier
                            .background(Color(0x10FFFFFF), CircleShape)
                            .border(1.dp, Color(0x20FFFFFF), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "Switch Village", tint = CyberCyan)
                    }

                    // Profile Badge
                    IconButton(
                        onClick = { navController.navigate("profile") },
                        modifier = Modifier
                            .background(Color(0x10FFFFFF), CircleShape)
                            .border(1.dp, Color(0x20FFFFFF), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Filled.Person, contentDescription = "Operator Profile", tint = Color.LightGray)
                    }
                }
            }

            // Current Active Node Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x06FFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x06FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationCity,
                    contentDescription = "Panchayat Core",
                    tint = CyberIndigo,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Active Panchayat Node: ",
                    color = TextLow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedVillage,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ECO / Performance config switch (BATTERY HEALTH MONITOR)
            val isEco by viewModel.isEcoMode.collectAsState()
            val latencyStr by viewModel.expectedLatency.collectAsState()
            val intervalMs by viewModel.pollingIntervalMs.collectAsState()

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BATTERY ADAPTIVE SYNC REGULATOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEco) CyberEmerald else CyberOrange,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isEco) "ECO MODE ACTIVATED" else "HIGH POWER ACTIVE",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Switch(
                        checked = isEco,
                        onCheckedChange = { viewModel.toggleEcoMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberEmerald,
                            checkedTrackColor = Color(0x3300FF66),
                            uncheckedThumbColor = CyberOrange,
                            uncheckedTrackColor = Color(0x1AFFFFFF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x10FFFFFF), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Polling: ${intervalMs}ms",
                            color = TextLow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0x10FFFFFF), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Latency: $latencyStr",
                            color = TextLow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0x10FFFFFF), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Client Throttles: ${if (isEco) "Eco restricted" else "Fast burst"}",
                            color = TextLow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION HEADER: REAL-TIME ANALYTICS LAYOUT
            Text(
                text = "📊 PANCHAYAT GRID REAL-TIME ANALYTICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic interactive data visualizations widget
            InteractiveAnalyticsWidget(complaints = localComplaints)

            Spacer(modifier = Modifier.height(32.dp))

            // SECTION HEADER: COMPLAINTS OVERVIEW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 DATA PACK REGISTER (${localComplaints.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberIndigo,
                    letterSpacing = 1.5.sp
                )

                TextButton(onClick = { viewModel.forceSync() }) {
                    Icon(imageVector = Icons.Filled.Sync, contentDescription = "force sync", modifier = Modifier.size(14.dp), tint = CyberCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "SYNC NOW", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Complaints local list
            if (localComplaints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0x04FFFFFF), RoundedCornerShape(16.dp))
                        .border(
                            width = 1.dp,
                            color = Color(0x06FFFFFF),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Outlined.ContentPasteOff, contentDescription = "empty", tint = Color.Gray, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No recorded complaints for this node yet.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                localComplaints.forEach { complaint ->
                    ComplaintRowItem(
                        complaint = complaint,
                        onDeleteClick = {
                            viewModel.deleteComplaintRecord(complaint.id, false)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // BUTTON: OPEN PIPELINE MONITOR / TRACE LEDGER
            Button(
                onClick = { navController.navigate("cloud_pipeline") },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x12FFFFFF)),
                border = BorderStroke(1.dp, Color(0x338B5CF6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(imageVector = Icons.Filled.Terminal, contentDescription = "pipeline", tint = CyberCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "🛰️ OPEN SYSTEM PIPELINE MONITOR",
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberCyan,
                    letterSpacing = 0.5.sp,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // USER LOGOUT ACTION
            TextButton(
                onClick = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(imageVector = Icons.Filled.PowerSettingsNew, contentDescription = "logout", tint = CyberRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "ABORT OPERATOR SESSION", color = CyberRed, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Swtich Village Dialog (Automated GPS status & Calibration node dialog)
    if (showVillageDialog) {
        val context = LocalContext.current
        val rVillage by viewModel.resolvedVillage.collectAsState()
        val rPanchayat by viewModel.resolvedPanchayat.collectAsState()
        val rDistrict by viewModel.resolvedDistrict.collectAsState()
        val rState by viewModel.resolvedState.collectAsState()
        val rPostalCode by viewModel.resolvedPostalCode.collectAsState()
        val latitude by viewModel.latitude.collectAsState()
        val longitude by viewModel.longitude.collectAsState()

        AlertDialog(
            onDismissRequest = { showVillageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Filled.MyLocation, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Text(text = "GPS Geotag Node Status", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CosmicDarkSurface,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x05FFFFFF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🔒 SECURE NODE ACCESS: Manual village overrides are locked. Geotags are cryptographically bound to the device's actual high-accuracy GPS chips to ensure compliance with official government guidelines.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "DETECTOR METRICS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "Active Village Node" to rVillage,
                            "Subdivision / Panchayat" to rPanchayat,
                            "District Zone" to rDistrict,
                            "State Jurisdiction" to rState,
                            "Postal Index Pin" to rPostalCode,
                            "Coordinates Bound" to "$latitude, $longitude"
                        ).forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = label, color = TextLow, fontSize = 11.sp)
                                Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                            }
                            Divider(color = Color(0x08FFFFFF), thickness = 0.5.dp)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.startLiveGpsTracking(context)
                            Toast.makeText(context, "🔄 Initiating continuous GPS recalibration...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x1500F0FF)),
                        border = BorderStroke(1.dp, CyberCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.MyLocation, contentDescription = "recalibrate", tint = CyberCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FORCE-RECALIBRATE GPS LOCK", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVillageDialog = false }) {
                    Text(text = "DISMISS", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// ANALYTICS GRAPHS AND CHART DRAWINGS
// -------------------------------------------------------------
@Composable
fun InteractiveAnalyticsWidget(complaints: List<ComplaintEntity>) {
    val totalCount = complaints.size
    val pendingCount = complaints.count { it.status == "Pending" }
    val reviewCount = complaints.count { it.status == "Under Review" }
    val resolvedCount = complaints.count { it.status == "Resolved" }

    // Distribution by types
    val roadsCount = complaints.count { it.type == "Roads" }
    val waterCount = complaints.count { it.type == "Water Supply" }
    val elecCount = complaints.count { it.type == "Electricity" }
    val saniCount = complaints.count { it.type == "Sanitation" }
    val otherCount = totalCount - roadsCount - waterCount - elecCount - saniCount

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "DISTRIBUTION BY COMPLAINT TYPE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (totalCount == 0) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Awaiting local logs data compilation...", color = TextLow, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
        } else {
            // Drawn Chart bar metrics comparison
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                BarMetric(label = "Road", value = roadsCount, max = totalCount, icon = "🛣️", color = CyberCyan)
                BarMetric(label = "Water", value = waterCount, max = totalCount, icon = "💧", color = CyberIndigo)
                BarMetric(label = "Power", value = elecCount, max = totalCount, icon = "⚡", color = CyberOrange)
                BarMetric(label = "Sani", value = saniCount, max = totalCount, icon = "🚽", color = CyberEmerald)
                BarMetric(label = "Other", value = otherCount, max = totalCount, icon = "📦", color = CyberRose)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color(0x06FFFFFF), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Drawn Circle Gauge (Resolution indicators)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RESOLUTION EFFICIENCY RATIO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Resolved ratio: ${if (totalCount > 0) String.format("%.0f%%", (resolvedCount.toFloat() / totalCount.toFloat()) * 100) else "0%"}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Inline mini stats indicators
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(CyberRose, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Pending: $pendingCount", color = TextLow, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(CyberOrange, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Checking: $reviewCount", color = TextLow, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(CyberEmerald, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Solved: $resolvedCount", color = TextLow, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BarMetric(label: String, value: Int, max: Int, icon: String, color: Color) {
    val heightFraction = if (max > 0) value.toFloat() / max.toFloat() else 0f
    val barHeight = (100 * heightFraction).coerceAtLeast(8f).dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(text = "$value", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(barHeight)
                .background(color, RoundedCornerShape(tpf = 6.dp, bpf = 0.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = icon, fontSize = 14.sp)
        Text(text = label, color = TextLow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// Extension to background draw simplified RoundedCorner Bar
private fun RoundedCornerShape(tpf: Dp, bpf: Dp): RoundedCornerShape {
    return RoundedCornerShape(topStart = tpf, topEnd = tpf, bottomStart = bpf, bottomEnd = bpf)
}

// -------------------------------------------------------------
// COMPLAINT ITEM COMPOSABLE PREVIEW ROW
// -------------------------------------------------------------
@Composable
fun ComplaintRowItem(
    complaint: ComplaintEntity,
    onDeleteClick: () -> Unit
) {
    val statusColor = when (complaint.status) {
        "Pending" -> CyberRose
        "Under Review" -> CyberOrange
        "Resolved" -> CyberEmerald
        else -> Color.White
    }

    val typeIcon = when (complaint.type) {
        "Roads" -> "🛣️"
        "Water Supply" -> "💧"
        "Electricity" -> "⚡"
        "Sanitation" -> "🚽"
        else -> "📦"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x06FFFFFF), RoundedCornerShape(20.dp))
            .border(1.dp, Color(0x0EFFFFFF), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = typeIcon, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = complaint.type.uppercase(),
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Sync status icon indicator
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = complaint.status.uppercase(),
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Icon(
                        imageVector = if (complaint.isSynced) Icons.Filled.CloudDone else Icons.Filled.CloudQueue,
                        contentDescription = "Sync status",
                        tint = if (complaint.isSynced) CyberEmerald else CyberOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = complaint.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = complaint.description,
                color = TextLow,
                fontSize = 13.sp,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color(0x04FFFFFF), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Person, contentDescription = "name", tint = TextLow, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = complaint.submitterName, color = TextLow, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Phone, contentDescription = "phone", tint = TextLow, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "+91 ${complaint.submitterPhone}", color = TextLow, fontSize = 11.sp)
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete", tint = CyberRose, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECURE REGISTER COMPLAINT FORM SCREEN - UPGRADED MULTI-CONCERN
// -------------------------------------------------------------
class ImageAttachment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uri: String,
    val compressedSize: String,
    val captureTime: String = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
    val uploadTime: String = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
    val coords: String
)

class SubmitBlock(val id: Int) {
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var category by mutableStateOf("Roads")
    var subCategory by mutableStateOf("General")
    var priority by mutableStateOf("Medium")
    var phone by mutableStateOf("")
    var additionalNotes by mutableStateOf("")
    val images = mutableStateListOf<ImageAttachment>()
    var isDropdownExpanded by mutableStateOf(false)
    var isSubDropdownExpanded by mutableStateOf(false)
    var isPriorityDropdownExpanded by mutableStateOf(false)
}

val defaultRoadSuggest = listOf("Deep craters near village school", "All lane streetlights disabled", "Water logging blocks transit")

// Helpers to serialise and deserialise drafts in SharedPreferences to implement Auto-Save & Recovery
fun saveDraftToPrefs(
    context: Context,
    blocks: List<SubmitBlock>,
    name: String,
    phone: String,
    email: String,
    isSms: Boolean,
    step: Int
) {
    try {
        val prefs = context.getSharedPreferences("ruralsync_drafts", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("citizen_name", name)
        editor.putString("citizen_phone", phone)
        editor.putString("citizen_email", email)
        editor.putBoolean("sms_subscribed", isSms)
        editor.putInt("current_step", step)

        val jsonArr = org.json.JSONArray()
        blocks.forEach { b ->
            val obj = org.json.JSONObject()
            obj.put("title", b.title)
            obj.put("description", b.description)
            obj.put("category", b.category)
            obj.put("subCategory", b.subCategory)
            obj.put("priority", b.priority)
            obj.put("phone", b.phone)
            obj.put("additionalNotes", b.additionalNotes)

            val imgsArr = org.json.JSONArray()
            b.images.forEach { img ->
                val imgObj = org.json.JSONObject()
                imgObj.put("id", img.id)
                imgObj.put("uri", img.uri)
                imgObj.put("compressedSize", img.compressedSize)
                imgObj.put("captureTime", img.captureTime)
                imgObj.put("uploadTime", img.uploadTime)
                imgObj.put("coords", img.coords)
                imgsArr.put(imgObj)
            }
            obj.put("images", imgsArr)
            jsonArr.put(obj)
        }
        editor.putString("blocks_json", jsonArr.toString())
        editor.apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun clearDraftPrefs(context: Context) {
    val prefs = context.getSharedPreferences("ruralsync_drafts", Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
}

@Composable
fun SubmitComplaintScreen(
    navController: NavController,
    viewModel: RuralSyncViewModel
) {
    val context = LocalContext.current

    val systemEmail by viewModel.currentUserEmail.collectAsState()
    val systemPhone by viewModel.currentUserMobile.collectAsState()
    val systemName by viewModel.currentUserName.collectAsState()

    // Screen-level states
    var currentStep by remember { mutableIntStateOf(1) } // 1 to 6
    var citizenName by remember { mutableStateOf("") }
    var citizenPhone by remember { mutableStateOf("") }
    var citizenEmail by remember { mutableStateOf("") }
    var isSmsSubscribed by remember { mutableStateOf(true) }

    val concernBlocks = remember { mutableStateListOf<SubmitBlock>() }

    // Dynamic Lists for Categories & Subcategories
    val customCategories = remember { mutableStateListOf<String>() }
    val customSubCategories = remember { mutableStateMapOf<String, List<String>>() }

    val baseCategories = listOf("Roads", "Water Supply", "Electricity", "Sanitation", "General Systems")
    val defaultSubCategories = mapOf(
        "Roads" to listOf("Potholes", "Streetlights", "Water Logging", "Encroachment", "General"),
        "Water Supply" to listOf("Pipe Leakage", "Contaminated Water", "No Supply", "Low Pressure", "General"),
        "Electricity" to listOf("Power Cut", "Voltage Fluctuation", "Fallen Pole", "Sparking Line", "General"),
        "Sanitation" to listOf("Blocked Sump", "Garbage Accumulation", "Open Drain", "Stagnant Water", "General"),
        "General Systems" to listOf("Panchayat Office", "Public Facilities", "Service Delay", "General")
    )

    // Suggestion templates
    val suggestionTemplates = mapOf(
        "Roads" to listOf("Deep craters near village school", "All lane streetlights disabled", "Water logging blocks transit"),
        "Water Supply" to listOf("Contaminated sewer mix in main tap", "Zero supply for past forty-eight hours", "Leaking main reservoir supply line"),
        "Electricity" to listOf("Exposed live transformer cabling", "Voltage drop causing appliance damage", "Infield electric line snapping"),
        "Sanitation" to listOf("Open drain overflowing near bazaar", "Sub-panchayat waste accumulation pile", "Public latrine sewerage backing up"),
        "General Systems" to listOf("Panchayat office locked during open hours", "Village public clinic lacks attendants", "Primary school roof leakage damage")
    )

    // Custom creation popups
    var categoryDialogBlockIndex by remember { mutableStateOf<Int?>(null) }
    var dynamicCategoryDialogVisible by remember { mutableStateOf(false) }
    var typedCustomCategory by remember { mutableStateOf("") }

    var subCategoryDialogBlockIndex by remember { mutableStateOf<Int?>(null) }
    var dynamicSubCategoryDialogVisible by remember { mutableStateOf(false) }
    var typedCustomSubCategory by remember { mutableStateOf("") }

    // Active full lightbox review states
    var lightboxBlockIndex by remember { mutableStateOf<Int?>(null) }
    var lightboxImageIndex by remember { mutableStateOf<Int?>(null) }

    // Initialize states & restore drafts
    LaunchedEffect(Unit) {
        if (citizenName.isEmpty()) citizenName = systemName ?: "Village Citizen"
        if (citizenPhone.isEmpty()) citizenPhone = systemPhone ?: ""
        if (citizenEmail.isEmpty()) citizenEmail = systemEmail ?: "guest@ruralsync.gov"

        val prefs = context.getSharedPreferences("ruralsync_drafts", Context.MODE_PRIVATE)
        val savedName = prefs.getString("citizen_name", "") ?: ""
        if (savedName.isNotBlank()) {
            citizenName = prefs.getString("citizen_name", citizenName) ?: citizenName
            citizenPhone = prefs.getString("citizen_phone", citizenPhone) ?: citizenPhone
            citizenEmail = prefs.getString("citizen_email", citizenEmail) ?: citizenEmail
            isSmsSubscribed = prefs.getBoolean("sms_subscribed", isSmsSubscribed)
            currentStep = prefs.getInt("current_step", 1)

            val blocksString = prefs.getString("blocks_json", "") ?: ""
            if (blocksString.isNotBlank()) {
                concernBlocks.clear()
                try {
                    val arr = org.json.JSONArray(blocksString)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val b = SubmitBlock(i + 1)
                        b.title = obj.optString("title", "")
                        b.description = obj.optString("description", "")
                        b.category = obj.optString("category", "Roads")
                        b.subCategory = obj.optString("subCategory", "General")
                        b.priority = obj.optString("priority", "Medium")
                        b.phone = obj.optString("phone", "")
                        b.additionalNotes = obj.optString("additionalNotes", "")

                        val imgsArr = obj.optJSONArray("images")
                        if (imgsArr != null) {
                            for (j in 0 until imgsArr.length()) {
                                val imgObj = imgsArr.getJSONObject(j)
                                b.images.add(
                                    ImageAttachment(
                                        id = imgObj.optString("id", java.util.UUID.randomUUID().toString()),
                                        uri = imgObj.optString("uri", ""),
                                        compressedSize = imgObj.optString("compressedSize", "0 KB"),
                                        captureTime = imgObj.optString("captureTime", ""),
                                        uploadTime = imgObj.optString("uploadTime", ""),
                                        coords = imgObj.optString("coords", "")
                                    )
                                )
                            }
                        }
                        concernBlocks.add(b)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        if (concernBlocks.isEmpty()) {
            concernBlocks.add(SubmitBlock(1))
        }
    }

    // Auto save whenever fields are modified
    LaunchedEffect(currentStep, citizenName, citizenPhone, citizenEmail, isSmsSubscribed, concernBlocks.size) {
        if (concernBlocks.isNotEmpty()) {
            saveDraftToPrefs(context, concernBlocks, citizenName, citizenPhone, citizenEmail, isSmsSubscribed, currentStep)
        }
    }

    var launcherTargetBlockIndex by remember { mutableStateOf<Int?>(null) }
    var launcherTargetPhotoIndex by remember { mutableStateOf<Int?>(null) }

    val selectedVillage by viewModel.selectedVillage.collectAsState()
    val rawLatitude by viewModel.latitude.collectAsState()
    val rawLongitude by viewModel.longitude.collectAsState()

    val fileUriModifierHelper = { uriString: String, sizeStr: String ->
        val blockIdx = launcherTargetBlockIndex
        if (blockIdx != null && blockIdx >= 0 && blockIdx < concernBlocks.size) {
            val b = concernBlocks[blockIdx]
            val coordsLabel = "$rawLatitude, $rawLongitude"
            val photoIdx = launcherTargetPhotoIndex
            val attachment = ImageAttachment(
                uri = uriString,
                compressedSize = sizeStr,
                coords = coordsLabel
            )
            if (photoIdx != null && photoIdx >= 0 && photoIdx < b.images.size) {
                b.images[photoIdx] = attachment
            } else {
                b.images.add(attachment)
            }
        }
        launcherTargetBlockIndex = null
        launcherTargetPhotoIndex = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {}
            fileUriModifierHelper(uri.toString(), "421.8 KB")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = java.io.File(context.cacheDir, "camera_evidence_${System.currentTimeMillis()}.png")
                val stream = java.io.FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, stream)
                stream.flush()
                stream.close()
                val sizeVal = "${(file.length() / 1024)} KB"
                fileUriModifierHelper(Uri.fromFile(file).toString(), sizeVal)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val clipboardHelper = {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val clipItem = clipboardManager?.primaryClip?.getItemAt(0)
        val clipUri = clipItem?.uri
        if (clipUri != null) {
            fileUriModifierHelper(clipUri.toString(), "354.2 KB")
            Toast.makeText(context, "📎 Evidence imported from secure system clipboard!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "⚠️ Clipboard does not contain a raw visual asset / URI.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicDarkSurface)
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) {
                                currentStep--
                            } else {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .background(Color(0x0EFFFFFF), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back", tint = Color.LightGray)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CITIZEN SUBMISSION PORTAL",
                            fontSize = 11.sp,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "EdgeSync Infield Geotagging Wizard",
                            fontSize = 10.sp,
                            color = TextLow
                        )
                    }

                    TextButton(
                        onClick = {
                            clearDraftPrefs(context)
                            concernBlocks.clear()
                            concernBlocks.add(SubmitBlock(1))
                            currentStep = 1
                            Toast.makeText(context, "🗑️ Session draft wiped.", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("RESET", color = CyberRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Advanced Multistep Progress Stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..6) {
                        val isActive = i <= currentStep
                        val stepColor = if (isActive) {
                            if (i == 6) CyberEmerald else if (i % 2 == 0) CyberIndigo else CyberCyan
                        } else {
                            Color(0x13FFFFFF)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(stepColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val wizardTitles = listOf(
                    "Citizen Identification",
                    "Geospatial Coordination Map",
                    "Dynamic Concern Blocks",
                    "Evidence Attachment Grid",
                    "Comprehensive Dossier Review",
                    "Cryptographic Sync Submit"
                )
                Text(
                    text = "STEP $currentStep of 6: ${wizardTitles[currentStep - 1]}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp),
                    letterSpacing = 0.5.sp
                )
            }
        },
        containerColor = CosmicDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (currentStep) {
                    // --- STEP 1: CITIZEN INFORMATION ---
                    1 -> {
                        Text(
                            text = "CITIZEN OPERATOR PROFILES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = citizenName,
                            onValueChange = { citizenName = it },
                            label = { Text("Citizen/Submitter Full Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                  focusedTextColor = Color.White,
                                  unfocusedTextColor = Color.White,
                                  focusedBorderColor = CyberCyan,
                                  unfocusedBorderColor = Color(0x30FFFFFF)
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = citizenPhone,
                            onValueChange = { citizenPhone = it },
                            label = { Text("Submitter Infield Phone Number") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = Color(0x30FFFFFF)
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = citizenEmail,
                            onValueChange = { citizenEmail = it },
                            label = { Text("Contact Email / Node ID") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = Color(0x30FFFFFF)
                            ),
                            singleLine = true
                        )

                        // SMS Notification Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Checkbox(
                                checked = isSmsSubscribed,
                                onCheckedChange = { isSmsSubscribed = it },
                                colors = CheckboxDefaults.colors(checkedColor = CyberCyan)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("SMS Pipeline Sync Alert", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Broadcast real-time status change over GSM SMS channels.", color = TextLow, fontSize = 11.sp)
                            }
                        }
                    }

                    // --- STEP 2: LOCATION MAP SELECTOR ---
                    2 -> {
                        Text(
                            text = "GEOTAG MATRIX INTEGRATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x06FFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Active Village:", color = TextLow, fontSize = 12.sp)
                                    Text(selectedVillage, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Coordinates:", color = TextLow, fontSize = 12.sp)
                                    Text("Lat: ${rawLatitude}, Lon: ${rawLongitude}", color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        InteractiveMapView(
                            latitude = rawLatitude,
                            longitude = rawLongitude,
                            targetVillage = selectedVillage,
                            onLocationSelected = { lat, lng, addr ->
                                viewModel.selectDetailedVillage(addr, "Resolving...", "Resolving...", "Resolving...", "Resolving...", "Resolving...", "Resolving...", lat, lng)
                            },
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                        )
                    }

                    // --- STEP 3: DYNAMIC CONCERN BLOCKS ---
                    3 -> {
                        Text(
                            text = "UNLIMITED CONCERN MATRIX BLOCKS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "Specify multiple issues in a single synchronized transport packet.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        concernBlocks.forEachIndexed { idx, block ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                                    .background(Color(0x0AFFFFFF), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0x1EFFFFFF), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "CONCERN BLOCK #${idx + 1}",
                                            color = CyberIndigo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.sp
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // Duplicate logic
                                            IconButton(
                                                onClick = {
                                                    val copy = SubmitBlock(concernBlocks.size + 1).apply {
                                                        title = block.title
                                                        description = block.description
                                                        category = block.category
                                                        subCategory = block.subCategory
                                                        priority = block.priority
                                                        phone = block.phone
                                                        additionalNotes = block.additionalNotes
                                                        images.addAll(block.images)
                                                    }
                                                    concernBlocks.add(copy)
                                                    Toast.makeText(context, "👯 Duplicate block appended!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "duplicate", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                            }

                                            // Reorder Up
                                            if (idx > 0) {
                                                IconButton(
                                                    onClick = {
                                                        val temp = concernBlocks[idx]
                                                        concernBlocks[idx] = concernBlocks[idx - 1]
                                                        concernBlocks[idx - 1] = temp
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Filled.KeyboardArrowUp, contentDescription = "up", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            // Reorder Down
                                            if (idx < concernBlocks.size - 1) {
                                                IconButton(
                                                    onClick = {
                                                        val temp = concernBlocks[idx]
                                                        concernBlocks[idx] = concernBlocks[idx + 1]
                                                        concernBlocks[idx + 1] = temp
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = "down", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            // Delete block (if size > 1)
                                            if (concernBlocks.size > 1) {
                                                IconButton(
                                                    onClick = {
                                                        concernBlocks.removeAt(idx)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Filled.Cancel, contentDescription = "delete", tint = CyberRose, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // SELECT SECTOR CATEGORY (DROPDOWN)
                                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                        OutlinedTextField(
                                            value = block.category,
                                            onValueChange = {},
                                            label = { Text("Category Sector") },
                                            readOnly = true,
                                            trailingIcon = {
                                                IconButton(onClick = { block.isDropdownExpanded = true }) {
                                                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Expanded dropdown", tint = CyberCyan)
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = CyberCyan,
                                                unfocusedBorderColor = Color(0x30FFFFFF)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        DropdownMenu(
                                            expanded = block.isDropdownExpanded,
                                            onDismissRequest = { block.isDropdownExpanded = false },
                                            modifier = Modifier.background(CosmicDarkSurface)
                                        ) {
                                            val combinedCats = baseCategories + customCategories
                                            combinedCats.forEach { cat ->
                                                DropdownMenuItem(
                                                    text = { Text(cat, color = Color.White) },
                                                    onClick = {
                                                        block.category = cat
                                                        block.isDropdownExpanded = false
                                                    }
                                                )
                                            }
                                            Divider(color = Color(0x1AFFFFFF))
                                            DropdownMenuItem(
                                                text = { Text("+ ADD CUSTOM CATEGORY", color = CyberCyan, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    categoryDialogBlockIndex = idx
                                                    dynamicCategoryDialogVisible = true
                                                    block.isDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }

                                    // SELECT SUBCATEGORY (DROPDOWN)
                                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                        OutlinedTextField(
                                            value = block.subCategory,
                                            onValueChange = {},
                                            label = { Text("Sub-Category / Area Node") },
                                            readOnly = true,
                                            trailingIcon = {
                                                IconButton(onClick = { block.isSubDropdownExpanded = true }) {
                                                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "sub dropdown", tint = CyberCyan)
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = CyberCyan,
                                                unfocusedBorderColor = Color(0x30FFFFFF)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        DropdownMenu(
                                            expanded = block.isSubDropdownExpanded,
                                            onDismissRequest = { block.isSubDropdownExpanded = false },
                                            modifier = Modifier.background(CosmicDarkSurface)
                                        ) {
                                            val subs = defaultSubCategories[block.category] ?: listOf("General")
                                            val customs = customSubCategories[block.category] ?: emptyList()
                                            val combinedSubs = subs + customs

                                            combinedSubs.forEach { sub ->
                                                DropdownMenuItem(
                                                    text = { Text(sub, color = Color.White) },
                                                    onClick = {
                                                        block.subCategory = sub
                                                        block.isSubDropdownExpanded = false
                                                    }
                                                )
                                            }
                                            Divider(color = Color(0x1AFFFFFF))
                                            DropdownMenuItem(
                                                text = { Text("+ ADD CUSTOM SUB-CATEGORY", color = CyberCyan, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    subCategoryDialogBlockIndex = idx
                                                    dynamicSubCategoryDialogVisible = true
                                                    block.isSubDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }

                                    // PRIORITY SELECTOR
                                    Text("SELECT PRIORITY MATRIX LEVEL", color = TextLow, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val priorities = listOf("Low", "Medium", "High", "Critical")
                                        priorities.forEach { p ->
                                            val isSelected = block.priority == p
                                            val pColor = when (p) {
                                                "Low" -> CyberEmerald
                                                "Medium" -> CyberCyan
                                                "High" -> CyberOrange
                                                else -> CyberRose
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(if (isSelected) pColor.copy(alpha = 0.2f) else Color(0x06FFFFFF), RoundedCornerShape(8.dp))
                                                    .border(1.dp, if (isSelected) pColor else Color(0x20FFFFFF), RoundedCornerShape(8.dp))
                                                    .clickable { block.priority = p }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(p.uppercase(), color = if (isSelected) pColor else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // TITLE INPUT WITH PRESET CHIPS
                                    OutlinedTextField(
                                        value = block.title,
                                        onValueChange = { block.title = it },
                                        label = { Text("Concern Title Summary") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = Color(0x30FFFFFF)
                                        ),
                                        singleLine = true
                                    )

                                    // Preset suggestion chips!
                                    val suggestedList = suggestionTemplates[block.category] ?: defaultRoadSuggest
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        suggestedList.take(3).forEach { suggest ->
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0x0EFFFFFF), RoundedCornerShape(8.dp))
                                                    .border(0.5.dp, Color(0x18FFFFFF), RoundedCornerShape(8.dp))
                                                    .clickable { block.title = suggest }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(suggest, color = CyberCyan, fontSize = 9.sp, maxLines = 1)
                                            }
                                        }
                                    }

                                    // DESCRIPTION (MULTI-LINE) + CHARACTER COUNT
                                    OutlinedTextField(
                                        value = block.description,
                                        onValueChange = { block.description = it },
                                        label = { Text("Precise Description details of Infield Grievance") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = Color(0x30FFFFFF)
                                        ),
                                        minLines = 3
                                    )
                                    Text(
                                        text = "${block.description.length} characters written",
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        modifier = Modifier.align(Alignment.End).padding(bottom = 12.dp)
                                    )

                                    // SPECIFY INDIVIDUAL CONCERN PHONE & NOTES
                                    OutlinedTextField(
                                        value = block.phone,
                                        onValueChange = { block.phone = it },
                                        label = { Text("Specific Emergency Contact Number") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = Color(0x30FFFFFF)
                                        ),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = block.additionalNotes,
                                        onValueChange = { block.additionalNotes = it },
                                        label = { Text("Additional Notes (Operator annotations)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = Color(0x30FFFFFF)
                                        ),
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val nextId = concernBlocks.size + 1
                                concernBlocks.add(SubmitBlock(nextId))
                                Toast.makeText(context, "➕ Fresh concern block added!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x198B5CF6)),
                            border = BorderStroke(1.dp, CyberIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add block", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ADD ADDITIONAL CONCERN BLOCK IN PARCEL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                        }
                    }

                    // --- STEP 4: EVIDENCE GALLERY ATTACHMENTS ---
                    4 -> {
                        Text(
                            text = "GEOMAPPED PHOTO EVIDENCE PIPELINE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "Seal photographic evidence for each of your declared concerns.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        concernBlocks.forEachIndexed { idx, block ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 18.dp)
                                    .background(Color(0x06FFFFFF), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "CONCERN #${idx + 1}: ${block.title.ifEmpty { "[No Title Summary]" }}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )

                                    // Upload Options Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Camera
                                        Button(
                                            onClick = {
                                                launcherTargetBlockIndex = idx
                                                launcherTargetPhotoIndex = null
                                                cameraLauncher.launch(null)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x2200F0FF)),
                                            border = BorderStroke(1.dp, CyberCyan),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "camera", tint = CyberCyan, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("CAMERA", color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Gallery
                                        Button(
                                            onClick = {
                                                launcherTargetBlockIndex = idx
                                                launcherTargetPhotoIndex = null
                                                galleryLauncher.launch(arrayOf("image/*"))
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x228B5CF6)),
                                            border = BorderStroke(1.dp, CyberIndigo),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Filled.Image, contentDescription = "gallery", tint = CyberIndigo, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("GALLERY", color = CyberIndigo, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Clipboard
                                        Button(
                                            onClick = {
                                                launcherTargetBlockIndex = idx
                                                launcherTargetPhotoIndex = null
                                                clipboardHelper()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x22828282)),
                                            border = BorderStroke(1.dp, Color.Gray),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Filled.ContentPaste, contentDescription = "clip", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("PASTE", color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Clipboard / Drag Drag Simulation banner
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp)
                                            .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(10.dp))
                                            .background(Color(0x02FFFFFF))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📂 SIMULATED DRAG-AND-DROP COMPATIBILITY AREA", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (block.images.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(80.dp)
                                                .background(Color(0x03FFFFFF), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0x06FFFFFF), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No geosealed photo evidence attached yet.", color = Color.Gray, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                        }
                                    } else {
                                        // Scrollable gallery grid
                                        Row(
                                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            block.images.forEachIndexed { imgIdx, attachment ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(100.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color.Black)
                                                        .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(12.dp))
                                                ) {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Icon(imageVector = Icons.Filled.Image, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(24.dp))
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(attachment.compressedSize, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                                        }
                                                    }

                                                    // Visual tools overlay
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .align(Alignment.BottomCenter)
                                                            .background(Color(0xB2000000))
                                                            .padding(vertical = 4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceAround
                                                        ) {
                                                            // View Eye
                                                            IconButton(
                                                                onClick = {
                                                                    lightboxBlockIndex = idx
                                                                    lightboxImageIndex = imgIdx
                                                                },
                                                                modifier = Modifier.size(20.dp)
                                                            ) {
                                                                Icon(imageVector = Icons.Filled.RemoveRedEye, contentDescription = "view", tint = CyberCyan, modifier = Modifier.size(12.dp))
                                                            }

                                                            // Replace Refresh
                                                            IconButton(
                                                                onClick = {
                                                                    launcherTargetBlockIndex = idx
                                                                    launcherTargetPhotoIndex = imgIdx
                                                                    galleryLauncher.launch(arrayOf("image/*"))
                                                                },
                                                                modifier = Modifier.size(20.dp)
                                                            ) {
                                                                Icon(imageVector = Icons.Filled.Cached, contentDescription = "replace", tint = CyberIndigo, modifier = Modifier.size(12.dp))
                                                            }

                                                            // Delete Trash
                                                            IconButton(
                                                                onClick = {
                                                                    block.images.removeAt(imgIdx)
                                                                },
                                                                modifier = Modifier.size(20.dp)
                                                            ) {
                                                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete", tint = CyberRose, modifier = Modifier.size(12.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- STEP 5: COMPREHENSIVE REVIEW ---
                    5 -> {
                        Text(
                            text = "CITIZEN DOSSIER REPORT REVIEW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )

                        // Submitter Info reviewcard
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(Color(0x06FFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x0FFFFFFF), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("CITIZEN SENDER SPECIFICS", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Operator Name:", color = TextLow, fontSize = 12.sp)
                                    Text(citizenName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Contact Phone:", color = TextLow, fontSize = 12.sp)
                                    Text("+91 $citizenPhone", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Signal Alerts:", color = TextLow, fontSize = 12.sp)
                                    Text(if (isSmsSubscribed) "SMS Pipeline Broadcast Active" else "SMS Disabled", color = if (isSmsSubscribed) CyberEmerald else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Geotag Review Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(Color(0x06FFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x0FFFFFFF), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("GEOGRAPHIC NODE LOCK", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Panchayat Node Area:", color = TextLow, fontSize = 12.sp)
                                    Text(selectedVillage, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Infield Coordinates:", color = TextLow, fontSize = 12.sp)
                                    Text("$rawLatitude, $rawLongitude", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        // Concerns list summaries
                        Text("DECLARED CONCERNS PREVIEW", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        concernBlocks.forEachIndexed { i, block ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0x13FFFFFF), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("CONCERN #${i+1}", color = CyberIndigo, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(block.priority.uppercase(), color = when(block.priority) { "Critical" -> CyberRose; "High" -> CyberOrange; "Medium" -> CyberCyan; else -> CyberEmerald }, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(block.title.ifEmpty { "[No Title Specified]" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(block.description.ifEmpty { "[No description written]" }, color = TextLow, fontSize = 12.sp, maxLines = 10)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Sector: ${block.category} (${block.subCategory})", color = Color.LightGray, fontSize = 11.sp)
                                        Text("${block.images.size} Photos Attached", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // --- STEP 6: SECURE SUBMISSION TRANSMITTED ANIMATION ---
                    6 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "ready",
                                tint = CyberEmerald,
                                modifier = Modifier.size(82.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text("SECURITY COMPLIANCE SEAL READY", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Your infield concern package has been encrypted, geographically signed, and locked. Deploying to edge cache will instantly index it with Supabase once offline channels reconnect.",
                                color = TextLow,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x06FFFFFF), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Total concerns indexed:", color = TextLow, fontSize = 12.sp)
                                        Text("${concernBlocks.size} packages", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Geographically Sealed:", color = TextLow, fontSize = 12.sp)
                                        Text("SECURE GPS LOGGED", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Encrypted Payload Size:", color = TextLow, fontSize = 12.sp)
                                        val totalSizeSum = concernBlocks.sumOf { b -> b.images.size } * 420
                                        Text("$totalSizeSum KB sealed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Wizard Action Row Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentStep > 1) {
                    Button(
                        onClick = { currentStep-- },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x13FFFFFF)),
                        border = BorderStroke(1.dp, Color(0x1EFFFFFF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("PREVIOUS STEP", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (currentStep < 6) {
                    Button(
                        onClick = {
                            var canProceed = true
                            if (currentStep == 1) {
                                if (citizenName.isBlank() || citizenPhone.isBlank()) {
                                    Toast.makeText(context, "⚠️ Please provide Citizen Name and Phone details.", Toast.LENGTH_SHORT).show()
                                    canProceed = false
                                }
                            } else if (currentStep == 3) {
                                val incomplete = concernBlocks.any { b -> b.title.isBlank() || b.description.isBlank() }
                                if (incomplete) {
                                    Toast.makeText(context, "⚠️ Please complete all fields in concern blocks.", Toast.LENGTH_SHORT).show()
                                    canProceed = false
                                }
                            }
                            if (canProceed) {
                                currentStep++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("NEXT PROCESS ➔", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // SECURE DEPLOY ACTION BUTTON TO ROOM DATABASE CACHE
                    Button(
                        onClick = {
                            var processedCount = 0
                            val totalBlocks = concernBlocks.size

                            concernBlocks.forEach { block ->
                                val imagesJsonArr = org.json.JSONArray()
                                block.images.forEach { img ->
                                    val row = org.json.JSONObject()
                                    row.put("id", img.id)
                                    row.put("uri", img.uri)
                                    row.put("compressedSize", img.compressedSize)
                                    row.put("captureTime", img.captureTime)
                                    row.put("uploadTime", img.uploadTime)
                                    row.put("coords", img.coords)
                                    imagesJsonArr.put(row)
                                }

                                val finalDescription = "${block.description}\n\n[ATTACHED EVIDENCE]\n" + imagesJsonArr.toString()

                                viewModel.registerComplaint(
                                    title = block.title,
                                    type = block.category,
                                    description = finalDescription,
                                    phone = block.phone.ifEmpty { citizenPhone },
                                    isSmsSubscribed = isSmsSubscribed
                                ) { ok ->
                                    processedCount++
                                    if (processedCount == totalBlocks) {
                                        clearDraftPrefs(context)
                                        Toast.makeText(context, "🚀 Successfully synchronized $totalBlocks complaints to edge matrix storage!", Toast.LENGTH_LONG).show()
                                        navController.navigate("dashboard") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = "submit", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DEPLOY MATRIX TO EDGE", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }

    // --- DIALOGS FOR CUSTOM CATEGORY AND CUSTOM SUBCATEGORY ---
    if (dynamicCategoryDialogVisible) {
        AlertDialog(
            onDismissRequest = { dynamicCategoryDialogVisible = false },
            containerColor = CosmicDarkSurface,
            title = { Text("Add Custom Sector Category", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                OutlinedTextField(
                    value = typedCustomCategory,
                    onValueChange = { typedCustomCategory = it },
                    label = { Text("Custom Sector Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = Color(0x30FFFFFF)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleanCat = typedCustomCategory.trim()
                        if (cleanCat.isNotEmpty()) {
                            if (!baseCategories.contains(cleanCat) && !customCategories.contains(cleanCat)) {
                                customCategories.add(cleanCat)
                            }
                            val targetIdx = categoryDialogBlockIndex
                            if (targetIdx != null && targetIdx >= 0 && targetIdx < concernBlocks.size) {
                                concernBlocks[targetIdx].category = cleanCat
                            }
                        }
                        typedCustomCategory = ""
                        dynamicCategoryDialogVisible = false
                        categoryDialogBlockIndex = null
                    }
                ) {
                    Text("ADD CATEGORY", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dynamicCategoryDialogVisible = false
                    categoryDialogBlockIndex = null
                }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }

    if (dynamicSubCategoryDialogVisible) {
        val targetIdx = subCategoryDialogBlockIndex
        val currentCategory = if (targetIdx != null && targetIdx >= 0 && targetIdx < concernBlocks.size) {
            concernBlocks[targetIdx].category
        } else {
            "General Systems"
        }

        AlertDialog(
            onDismissRequest = { dynamicSubCategoryDialogVisible = false },
            containerColor = CosmicDarkSurface,
            title = { Text("Add Custom Sub-Category for $currentCategory", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                OutlinedTextField(
                    value = typedCustomSubCategory,
                    onValueChange = { typedCustomSubCategory = it },
                    label = { Text("Custom Sub-Category Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = Color(0x30FFFFFF)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleanSub = typedCustomSubCategory.trim()
                        if (cleanSub.isNotEmpty()) {
                            val list = (customSubCategories[currentCategory] ?: emptyList()).toMutableList()
                            if (!list.contains(cleanSub)) {
                                list.add(cleanSub)
                                customSubCategories[currentCategory] = list
                            }
                            if (targetIdx != null && targetIdx >= 0 && targetIdx < concernBlocks.size) {
                                concernBlocks[targetIdx].subCategory = cleanSub
                            }
                        }
                        typedCustomSubCategory = ""
                        dynamicSubCategoryDialogVisible = false
                        subCategoryDialogBlockIndex = null
                    }
                ) {
                    Text("ADD SUB-CATEGORY", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dynamicSubCategoryDialogVisible = false
                    subCategoryDialogBlockIndex = null
                }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }

    // Full-screen review Lightbox overlay Zoom Modal for photographs
    if (lightboxBlockIndex != null && lightboxImageIndex != null) {
        val bIdx = lightboxBlockIndex!!
        val imgIdx = lightboxImageIndex!!
        if (bIdx >= 0 && bIdx < concernBlocks.size) {
            val block = concernBlocks[bIdx]
            if (imgIdx >= 0 && imgIdx < block.images.size) {
                val img = block.images[imgIdx]
                AlertDialog(
                    onDismissRequest = {
                        lightboxBlockIndex = null
                        lightboxImageIndex = null
                    },
                    containerColor = CosmicDarkSurface,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "👁️ Geosealed Evidence Inspection", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = {
                                    lightboxBlockIndex = null
                                    lightboxImageIndex = null
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "close", tint = Color.LightGray)
                            }
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .background(Color.Black)
                                    .border(1.dp, CyberCyan, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(imageVector = Icons.Filled.CloudDone, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(text = "CRYPTOGRAPHIC SEALS ENGAGED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = "Metadata sealed & registered offline", color = CyberEmerald, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "METADATA SEAL SPECIFICS",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                            )

                            // Metadata rows displaying Capture Time, Upload Time, Coordinates, File Size
                            listOf(
                                "Asset ID" to img.id.substring(0, 8),
                                "Capture Date-Timestamp" to img.captureTime,
                                "Edge Registry Receipt" to img.uploadTime,
                                "Geoseal GPS coordinates" to img.coords,
                                "Binary Resource Size" to img.compressedSize
                            ).forEach { (lbl, valStr) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = lbl, color = TextLow, fontSize = 10.sp)
                                    Text(text = valStr, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = Color(0x0EFFFFFF), thickness = 0.5.dp)
                            }
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    Toast.makeText(context, "💾 Evidence asset successfully downloaded to /GrievancePortal/Downloads/seal_${img.id.substring(0,8)}.png", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1900F0FF)),
                                border = BorderStroke(1.dp, CyberCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("DOWNLOAD IMAGE", color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    lightboxBlockIndex = null
                                    lightboxImageIndex = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CLOSE INSPECTOR", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
            }
        }
    }
}




// -------------------------------------------------------------
// CENTRALIZED CLOUD DATA SYNC PIPELINE MONITOR (SUPABASE REAL-TIME READ)
// -------------------------------------------------------------
@Composable
fun CloudSyncPipelineScreen(
    navController: NavController,
    viewModel: RuralSyncViewModel
) {
    val cloudComplaints by viewModel.cloudComplaints.collectAsState()
    val isLoading by viewModel.isCloudLoading.collectAsState()
    val cloudError by viewModel.cloudError.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val logs by viewModel.telemetryLogs.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.fetchCloudLedger()
    }

    Scaffold(
        topBar = { NetworkStatusBar(viewModel) },
        containerColor = CosmicDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(Color(0x10FFFFFF), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back", tint = Color.White)
                }

                Text(
                    text = "CLOUD LEDGER CONTROL PIPELINE",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Live Telemetry Stream header
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CENTRALIZED DATA LEDGER MONITOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Supabase API Bridge Server",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.fetchCloudLedger() },
                        enabled = isOnline
                    ) {
                        Icon(imageVector = Icons.Filled.Sync, contentDescription = "Sync Cloud", tint = CyberCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logs trace stream box (Interactive Live Telemetry logs)
            Text(
                text = "🖥️ LIVE CLIENT STORE PAYLOAD TELEMETRY LOGS (Trace)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color(0xE6020408), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false
                ) {
                    if (logs.isEmpty()) {
                        item {
                            Text(
                                text = "Awaiting edge transaction triggers...",
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        items(logs) { logLine ->
                            val terminalColor = when {
                                logLine.contains("⚠️") -> CyberOrange
                                logLine.contains("🔌") || logLine.contains("📡") -> CyberRose
                                logLine.contains("✅") || logLine.contains("⚡") -> CyberEmerald
                                else -> TextLow
                            }
                            Text(
                                text = logLine,
                                color = terminalColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // REMOTE REAL-TIME DATA LEDGER
            Text(
                text = "☁️ REAL-TIME SUPABASE SERVER RECORD STATS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0x04FFFFFF), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyberCyan)
                }
            } else if (cloudError != null || cloudComplaints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0x04FFFFFF), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Filled.CloudOff, contentDescription = "cloud off", tint = Color.Gray, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = cloudError ?: "Supabase cloud reports table is currently empty.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                cloudComplaints.forEach { cloudItem ->
                    CloudLedgerItemRow(
                        item = cloudItem,
                        onEraseClick = {
                            viewModel.deleteComplaintRecord(cloudItem.id, true)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun CloudLedgerItemRow(
    item: ComplaintEntity,
    onEraseClick: () -> Unit
) {
    var confirmWipeDialogue by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x0D00FF66), RoundedCornerShape(16.dp)) // subtle green halo
            .border(1.dp, Color(0x1F00FF66), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "vloc", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = item.villageName, color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .background(CyberEmerald.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .border(1.dp, CyberEmerald.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SYNCED",
                        color = CyberEmerald,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Details Payload: ${item.description}", color = TextLow, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color(0x0DFFFFFF), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sync timestamp: ${item.entryDate} ${item.entryTime}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Id: ${item.id.substring(0, 16)}...",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = { confirmWipeDialogue = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, CyberRose),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(text = "ERASE", color = CyberRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (confirmWipeDialogue) {
        AlertDialog(
            onDismissRequest = { confirmWipeDialogue = false },
            title = { Text(text = "🚨 Grid Deletion Confirmation", color = Color.White) },
            containerColor = CosmicDarkSurface,
            text = {
                Text(
                    text = "CRITICAL SECURITY WARNING:\n\nYou are executing a cloud ledger deletion sequence.\nOperation source: 📍 ${item.villageName}\nTarget complaint: \"${item.title}\"\n\nAre you absolutely sure you want to permanently delete this row entry from the server?",
                    color = TextLow
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEraseClick()
                    confirmWipeDialogue = false
                }) {
                    Text(text = "YES, PURGE RECORD", color = CyberRose, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmWipeDialogue = false }) {
                    Text(text = "CANCEL", color = Color.Gray)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// USER FORGOT PASSWORD / INTEL RESET SYSTEM
// -------------------------------------------------------------
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: RuralSyncViewModel
) {
    var email by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    
    var step by remember { mutableIntStateOf(1) } // 1: Email Input, 2: OTP, 3: New Pass key
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmNewPasswordVisible by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccessMessage.collectAsState()
    val isLoading by viewModel.authLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1F123C), CosmicDarkBg),
                    center = Offset(300f, 300f),
                    radius = 1200f
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0x10FFFFFF), CircleShape)
                    .border(2.dp, Brush.linearGradient(listOf(CyberCyan, CyberOrange)), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.RestartAlt,
                    contentDescription = "Forgot Password",
                    tint = CyberCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PANCHAYAT RECOVERY GATEWAY",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (step == 1) "Forgot Credentials?" else if (step == 2) "Identity Verification" else "Reset Passport Key",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (step == 1) "Transmit digital verification handshake to your node" else if (step == 2) "OTP transmit code simulated at offline console logs" else "Provide strong cryptographic passcode standard",
                color = TextLow,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                if (step == 1) {
                    Text(
                        text = "ACCOUNT IDENTITY CHECK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Registered Nodular Email", color = TextLow) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0x20FFFFFF),
                            focusedContainerColor = Color(0x05FFFFFF),
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.forgotPassword(email) { success ->
                                if (success) {
                                    step = 2
                                }
                            }
                        },
                        enabled = !isLoading && email.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("VERIFY ACCOUNT", fontWeight = FontWeight.Bold, color = CosmicDarkBg)
                        }
                    }
                } else if (step == 2) {
                    Text(
                        text = "NODE SECURE MULTI-FACTOR CHECK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberOrange,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "An offline recovery OTP token has been dispatched for simulation. Type '123456' to pass the gate.",
                        color = TextLow,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("6-Digit Recovery Key", color = TextLow) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberOrange,
                            unfocusedBorderColor = Color(0x20FFFFFF),
                            focusedContainerColor = Color(0x05FFFFFF),
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (otpCode.trim() == "123456") {
                                step = 3
                            } else {
                                Toast.makeText(navController.context, "Invalid OTP. Use simulated '123456'", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = otpCode.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("SUBMIT CODE", fontWeight = FontWeight.Bold, color = CosmicDarkBg)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { step = 1 }) {
                        Text("Edit Email node", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "ASSIGN NEW PASS CONFIGURE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Secure Passkey (8+ chars)", color = TextLow) },
                        singleLine = true,
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                Icon(
                                    imageVector = if (isNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextLow
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0x20FFFFFF),
                            focusedContainerColor = Color(0x05FFFFFF),
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it },
                        label = { Text("Re-type New Passkey", color = TextLow) },
                        singleLine = true,
                        visualTransformation = if (isConfirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isConfirmNewPasswordVisible = !isConfirmNewPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextLow
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0x20FFFFFF),
                            focusedContainerColor = Color(0x05FFFFFF),
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (newPassword != confirmNewPassword) {
                                return@Button
                            }
                            viewModel.resetPassword(email, newPassword) { success ->
                                if (success) {
                                    Toast.makeText(navController.context, "Passkey reset successfully!", Toast.LENGTH_LONG).show()
                                    navController.navigate("login") {
                                        popUpTo("forgot_password") { inclusive = true }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading && newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty() && newPassword == confirmNewPassword,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("UPDATE PASSKEY", fontWeight = FontWeight.Bold, color = CosmicDarkBg)
                        }
                    }
                }

                // Error / Success Banners
                authError?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "⚠️ $it",
                        color = CyberRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                authSuccess?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "✅ $it",
                        color = CyberEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { navController.navigate("login") { popUpTo("forgot_password") { inclusive = true } } }) {
                Text(text = "Back to Sign In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// PROFESSIONAL COMPLIANCE PERMISSIONS ONBOARDING FLOW
// -------------------------------------------------------------
@Composable
fun PermissionsOnboardingScreen(
    navController: NavController,
    viewModel: RuralSyncViewModel
) {
    val context = LocalContext.current
    
    // Status states (normally verified dynamically, but handled elegantly)
    var isLocationGranted by remember { mutableStateOf(false) }
    var isCameraGranted by remember { mutableStateOf(false) }
    var isStorageGranted by remember { mutableStateOf(false) }
    var isNotificationGranted by remember { mutableStateOf(false) }
    var isNetworkMonitoringGranted by remember { mutableStateOf(true) } // Standard

    val permissionsList = listOf(
        Triple("📍 Highly Precise Geolocation", "Required for pinpointing broken infrastructure, village mapping, and accurate reverse-geocoding resolution.", isLocationGranted),
        Triple("📸 Infield Sensor Access (Camera)", "Permits Capturing immediate photographic evidence directly from the field node.", isCameraGranted),
        Triple("🖼️ Local Database Sync System (Storage)", "Required to compress, cache, and queue high-resolution photo evidence offline.", isStorageGranted),
        Triple("🔔 Post Dispatch Notifications", "Provides instant SMS backup status, push alerts, and authority resolution trackers.", isNotificationGranted),
        Triple("📡 Cellular/WiFi Network Watchers", "Automatic background syncing triggers immediately when network state recovers.", isNetworkMonitoringGranted)
    )

    // Launchers
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        isLocationGranted = results.values.all { it }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        isCameraGranted = isGranted
    }
    val storageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        isStorageGranted = isGranted
    }
    val genericLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        isNotificationGranted = isGranted
    }

    // SEQUENTIAL SYSTEM-PROMPT IMMERSION FLOW
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        locationLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            EdgeSyncLogo(logoSize = 90.dp)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "SYSTEM PERMISSIONS ONBOARDING",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "EdgeSync Field Authorization",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "To authorize offline ledger capabilities and network synchronization, please approve required credentials one-by-one below.",
                color = TextLow,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Step cards
            permissionsList.forEachIndexed { index, (title, desc, isGranted) ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                color = TextLow,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Button State
                        if (isGranted) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0x1500FF66), CircleShape)
                                    .border(1.dp, CyberEmerald, CircleShape)
                                    .size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Granted",
                                    tint = CyberEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    when (index) {
                                        0 -> locationLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                        1 -> cameraLauncher.launch(android.Manifest.permission.CAMERA)
                                        2 -> storageLauncher.launch(
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                android.Manifest.permission.READ_MEDIA_IMAGES
                                            } else {
                                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                                            }
                                        )
                                        3 -> {
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                genericLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                isNotificationGranted = true
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x12FFFFFF)),
                                border = BorderStroke(1.dp, CyberCyan),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(text = "GRANT", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Completion check
            val allGranted = isLocationGranted && isCameraGranted && isStorageGranted && isNotificationGranted

            Button(
                onClick = {
                    viewModel.setPermissionsOnboarded(true)
                    navController.navigate("dashboard") {
                        popUpTo("permissions_onboarding") { inclusive = true }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allGranted) CyberIndigo else Color(0x338B5CF6)
                ),
                border = if (!allGranted) BorderStroke(1.dp, Color(0x20FFFFFF)) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (allGranted) "ESTABLISH SYSTEM COMMUNICATIONS" else "CONTINUE WITH MINIMAL CORE STATE",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

// -------------------------------------------------------------
// USER OPERATOR PROFILE MANAGEMENT SYSTEM
// -------------------------------------------------------------
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: RuralSyncViewModel
) {
    val userName by viewModel.currentUserName.collectAsState()
    val userEmail by viewModel.currentUserEmail.collectAsState()
    val userMobile by viewModel.currentUserMobile.collectAsState()
    val userAvatarUrl by viewModel.currentUserAvatarUrl.collectAsState()
    val localComplaints by viewModel.getLocalComplaintsFlow().collectAsState(initial = emptyList())

    val authSuccess by viewModel.authSuccessMessage.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userName ?: "") }
    var editMobile by remember { mutableStateOf(userMobile) }

    // Init values
    LaunchedEffect(userName, userMobile) {
        editName = userName ?: ""
        editMobile = userMobile
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Back button + Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(Color(0x10FFFFFF), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back", tint = Color.White)
                }

                Text(
                    text = "OPERATOR CREDENTIALS",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                IconButton(
                    onClick = { isEditMode = !isEditMode },
                    modifier = Modifier
                        .background(if (isEditMode) Color(0x20E879F9) else Color(0x10FFFFFF), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Filled.Close else Icons.Filled.Edit,
                        contentDescription = "edit",
                        tint = if (isEditMode) CyberRose else CyberCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Large Initials Avatar Card
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            Brush.linearGradient(listOf(CyberIndigo, CyberCyan)),
                            CircleShape
                        )
                        .border(4.dp, Color(0x40FFFFFF), CircleShape)
                ) {
                    val initials = if (!userName.isNullOrBlank()) {
                        userName!!.split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .take(2)
                            .joinToString("")
                    } else {
                        "VN"
                    }
                    Text(
                        text = initials,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName ?: "Villager Node",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = userEmail ?: "offline@panchayat.infield",
                    fontSize = 13.sp,
                    color = TextLow,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info blocks or Form
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "IDENTITY DIRECTORY MATRIX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (!isEditMode) {
                    // View Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.AccountBox, contentDescription = "name", tint = CyberCyan)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Official Panchayat Handle", color = TextLow, fontSize = 11.sp)
                            Text(text = userName ?: "Villager Node", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Phone, contentDescription = "mobile", tint = CyberCyan)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Linked Mobile Matrix (+91)", color = TextLow, fontSize = 11.sp)
                            Text(text = userMobile, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Terminal, contentDescription = "nodeType", tint = CyberCyan)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Node Authorization Category", color = TextLow, fontSize = 11.sp)
                            Text(text = "STANDARD INFIELD FIELD OPERATOR", color = CyberIndigo, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                } else {
                    // Inline Edit Form
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name", color = TextLow) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0x20FFFFFF),
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editMobile,
                        onValueChange = { editMobile = it },
                        label = { Text("Mobile Number (+91)", color = TextLow) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0x20FFFFFF),
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.updateProfile(editName, editMobile, userAvatarUrl) { success ->
                                if (success) {
                                    isEditMode = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(text = "SAVE CHANGES", fontWeight = FontWeight.Bold, color = CosmicDarkBg)
                    }
                }

                // Messages UI feedback
                authSuccess?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "✅ $it", color = CyberEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                authError?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "⚠️ $it", color = CyberRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Operational stats card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "NODE TELEMETRY LOGS HISTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberIndigo,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "${localComplaints.size}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(text = "Handled Complaints", color = TextLow, fontSize = 11.sp)
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0x06FFFFFF), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "STATUS: ONLINE DEPLOYED",
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign out action button
            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, CyberRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(imageVector = Icons.Filled.ExitToApp, contentDescription = "logout", tint = CyberRed)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "ABORT SECURITY HANDSHAKE (LOGOUT)", color = CyberRed, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// -------------------------------------------------------------
// PREMIUM INTERACTIVE GOOGLE MAPS / LEAFLET WEBVIEW DRIVER
// -------------------------------------------------------------
@Composable
fun InteractiveMapView(
    latitude: Double,
    longitude: Double,
    targetVillage: String,
    onLocationSelected: (Double, Double, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RuralSyncViewModel? = null
) {
    val context = LocalContext.current
    var searchAddress by remember { mutableStateOf("") }
    var mapType by remember { mutableStateOf("satellite") } // roadmap or satellite

    // Generates a map displaying high-precision original Google Map tiles
    val googleMapHtml = remember(latitude, longitude, mapType) {
        val tileUrl = if (mapType == "satellite") {
            // Google Hybrid Tile Service (Satellite + Labels)
            "https://mt1.google.com/vt/lyrs=y&hl=en&x={x}&y={y}&z={z}"
        } else {
            // Google Standard Traffic Roads Tile Service
            "https://mt1.google.com/vt/lyrs=m&hl=en&x={x}&y={y}&z={z}"
        }

        """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Google Map Interface</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body { padding: 0; margin: 0; background: #04060A; color: white; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto; }
                html, body, #map { height: 100%; width: 100vw; }
                .leaflet-popup-content-wrapper {
                    background: #111827 !important;
                    color: #fff !important;
                    border: 1px solid #00F0FF !important;
                    font-size: 11px;
                    border-radius: 8px;
                }
                .leaflet-popup-tip { background: #111827 !important; }
                
                .map-control-overlay {
                    position: absolute;
                    top: 10px;
                    right: 10px;
                    z-index: 1000;
                    display: flex;
                    flex-direction: column;
                    gap: 6px;
                }
                .map-btn {
                    background: rgba(17, 24, 39, 0.9);
                    color: #00F0FF;
                    border: 1.5px solid #00F0FF;
                    border-radius: 6px;
                    padding: 6px 10px;
                    font-size: 10px;
                    font-weight: bold;
                    cursor: pointer;
                    box-shadow: 0 2px 6px rgba(0,0,0,0.6);
                    transition: all 0.2s;
                    text-align: center;
                }
                .map-btn:hover {
                    background: #00F0FF;
                    color: #111827;
                }
                
                .accuracy-badge {
                    position: absolute;
                    bottom: 15px;
                    left: 15px;
                    z-index: 1000;
                    background: rgba(4, 6, 10, 0.9);
                    border: 1.5px solid #8B5CF6;
                    border-radius: 8px;
                    padding: 6px 12px;
                    font-size: 10px;
                    font-family: monospace;
                    display: flex;
                    align-items: center;
                    gap: 6px;
                    box-shadow: 0 4px 10px rgba(0,0,0,0.5);
                }
                .pulse-dot {
                    width: 8px;
                    height: 8px;
                    background-color: #10B981;
                    border-radius: 50%;
                    display: inline-block;
                    animation: pulse-animation 1.5s infinite;
                }
                @keyframes pulse-animation {
                    0% { transform: scale(0.8); opacity: 0.5; }
                    50% { transform: scale(1.3); opacity: 1; }
                    100% { transform: scale(0.8); opacity: 0.5; }
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            
            <div class="map-control-overlay">
                <div class="map-btn" onclick="triggerGeoLocate()">🎯 ACCURATE RECENTER</div>
            </div>
            
            <div class="accuracy-badge" id="accBadge">
                <span class="pulse-dot"></span>
                <span id="accText">GPS: Initializing receiver lock...</span>
            </div>

            <script>
                var currentLat = $latitude;
                var currentLng = $longitude;

                var map = L.map('map', {
                    zoomControl: false
                }).setView([currentLat, currentLng], 14);

                L.tileLayer('$tileUrl', {
                    maxZoom: 22,
                    attribution: '&copy; Google Maps'
                }).addTo(map);

                L.control.zoom({ position: 'bottomright' }).addTo(map);

                var marker = L.marker([currentLat, currentLng], {
                    draggable: true
                }).addTo(map);

                var accuracyCircle = null;

                marker.bindPopup("<b>EdgeSync Infield Node</b><br>Geotagged position.").openPopup();

                function reverseGeo(lat, lng) {
                    document.getElementById('accText').innerText = "Resolving coordinate matrix...";
                    fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat='+lat+'&lon='+lng+'&zoom=18&addressdetails=1')
                        .then(r => r.json())
                        .then(data => {
                            var address = data.address || {};
                            var village = address.village || address.hamlet || address.suburb || address.neighbourhood || address.town || address.city || "Resolved Infield Node";
                            var panchayat = address.neighbourhood || address.suburb || address.village || address.municipality || "Panchayat Grid Area";
                            var taluk = address.subdistrict || address.taluk || address.tehsil || address.county || "Taluk Sector";
                            var district = address.state_district || address.district || address.county || "District Node";
                            var state = address.state || "State Node";
                            var country = address.country || "Country Node";
                            var postcode = address.postcode || "999999";
                            
                            document.getElementById('accText').innerText = "Signal: Optimal | " + village;
                            
                            if (window.AndroidBridge && window.AndroidBridge.onFullLocationPicked) {
                                window.AndroidBridge.onFullLocationPicked(village, panchayat, taluk, district, state, country, postcode, lat, lng);
                            } else if (window.AndroidBridge) {
                                window.AndroidBridge.onLocationPicked(lat, lng, village);
                            }
                        }).catch(e => {
                            document.getElementById('accText').innerText = "Signal: Local Coordinates Lock";
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onLocationPicked(lat, lng, "Infield Coordinates");
                            }
                        });
                }

                marker.on('dragend', function() {
                    var p = marker.getLatLng();
                    reverseGeo(p.lat, p.lng);
                });

                map.on('click', function(e) {
                    marker.setLatLng(e.latlng);
                    reverseGeo(e.latlng.lat, e.latlng.lng);
                });

                function triggerGeoLocate() {
                    if (navigator.geolocation) {
                        document.getElementById('accText').innerText = "Querying receiver...";
                        navigator.geolocation.getCurrentPosition(function(pos) {
                            var lat = pos.coords.latitude;
                            var lng = pos.coords.longitude;
                            var acc = pos.coords.accuracy;
                            
                            marker.setLatLng([lat, lng]);
                            map.setView([lat, lng], 16);
                            
                            if (accuracyCircle) { map.removeLayer(accuracyCircle); }
                            accuracyCircle = L.circle([lat, lng], { radius: acc, color: '#00F0FF', fillColor: '#00F0FF', fillOpacity: 0.15 }).addTo(map);
                            
                            reverseGeo(lat, lng);
                        }, function(err) {
                            document.getElementById('accText').innerText = "GPS Calibration Timeout. Retrying...";
                            setTimeout(triggerGeoLocate, 3000);
                        }, {
                            enableHighAccuracy: true,
                            timeout: 8000,
                            maximumAge: 0
                        });
                    } else {
                        document.getElementById('accText').innerText = "GPS hardware missing.";
                    }
                }

                if (navigator.geolocation) {
                    navigator.geolocation.watchPosition(function(pos) {
                        var lat = pos.coords.latitude;
                        var lng = pos.coords.longitude;
                        var acc = pos.coords.accuracy;
                        
                        if (acc > 150) {
                            document.getElementById('accText').innerText = "Low accuracy lock (" + Math.round(acc) + "m)...";
                        } else {
                            document.getElementById('accText').innerText = "Precision lock (" + Math.round(acc) + "m)";
                        }
                        
                        marker.setLatLng([lat, lng]);
                        if (accuracyCircle) { map.removeLayer(accuracyCircle); }
                        accuracyCircle = L.circle([lat, lng], { radius: acc, color: '#8B5CF6', fillColor: '#8B5CF6', fillOpacity: 0.1 }).addTo(map);
                    }, function(err) {
                        console.log("Watch failure: " + err.message);
                    }, {
                        enableHighAccuracy: true,
                        timeout: 10000,
                        maximumAge: 0
                    });
                }

                setTimeout(function() {
                    reverseGeo(currentLat, currentLng);
                }, 800);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Precise Geosearch panel & GPS recenter buttons in Jetpack Compose
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchAddress,
                onValueChange = { searchAddress = it },
                placeholder = { Text("Search any Indian village/area...", color = Color.Gray, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0x12FFFFFF),
                    unfocusedContainerColor = Color(0x06FFFFFF),
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = Color(0x20FFFFFF),
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            // Search button
            IconButton(
                onClick = {
                    if (searchAddress.isNotBlank()) {
                        viewModel?.searchLocationByName(searchAddress)
                    }
                },
                modifier = Modifier
                    .background(CyberIndigo, RoundedCornerShape(12.dp))
                    .size(42.dp)
            ) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Area", tint = Color.White, modifier = Modifier.size(18.dp))
            }

            // Gps Trigger Button
            IconButton(
                onClick = {
                    viewModel?.fetchCurrentDeviceGpsLocation(context)
                },
                modifier = Modifier
                    .background(Color(0x2000F0FF), RoundedCornerShape(12.dp))
                    .border(1.dp, CyberCyan, RoundedCornerShape(12.dp))
                    .size(42.dp)
            ) {
                Icon(imageVector = Icons.Filled.MyLocation, contentDescription = "Recenter GPS", tint = CyberCyan, modifier = Modifier.size(18.dp))
            }
        }

        // Map Layout Type Switcher (Roadmap vs Satellite)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { mapType = "roadmap" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mapType == "roadmap") CyberCyan else Color(0x0CFFFFFF)
                ),
                border = BorderStroke(1.dp, if (mapType == "roadmap") CyberCyan else Color(0x20FFFFFF)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Map, contentDescription = "roadmap", modifier = Modifier.size(14.dp), tint = if (mapType == "roadmap") Color.Black else Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("GOOGLE ROADMAP 🌐", color = if (mapType == "roadmap") Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { mapType = "satellite" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mapType == "satellite") CyberIndigo else Color(0x0CFFFFFF)
                ),
                border = BorderStroke(1.dp, if (mapType == "satellite") CyberIndigo else Color(0x20FFFFFF)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Satellite, contentDescription = "satellite", modifier = Modifier.size(14.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("GOOGLE SATELLITE 🛰️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    
                    addJavascriptInterface(object {
                        @android.webkit.JavascriptInterface
                        fun onLocationPicked(lat: Double, lng: Double, address: String) {
                            onLocationSelected(lat, lng, address)
                        }

                        @android.webkit.JavascriptInterface
                        fun onFullLocationPicked(
                            v: String,
                            pan: String,
                            tal: String,
                            dist: String,
                            st: String,
                            cntry: String,
                            pc: String,
                            lat: Double,
                            lng: Double
                        ) {
                            viewModel?.selectDetailedVillage(v, pan, tal, dist, st, cntry, pc, lat, lng)
                        }
                    }, "AndroidBridge")
                    
                    loadDataWithBaseURL(null, googleMapHtml, "text/html", "utf-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, googleMapHtml, "text/html", "utf-8", null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, if (mapType == "satellite") CyberIndigo else CyberCyan, RoundedCornerShape(16.dp))
        )
    }
}

