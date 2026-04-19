package com.example.a216553_praavieen_rajj_nelson_lab04

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.a216553_praavieen_rajj_nelson_lab04.ui.theme.A216553_PRAAVIEEN_RAJJ_NELSON_LAB04Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── Task 2: Data Class ───────────────────────────────────────────────────────
data class UserProfile(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = ""
)

// Envelope data model for the budget envelopes list
data class Envelope(
    val title: String,
    val emoji: String,
    val budgetTotal: Float,
    val budgetSpent: Float
)

// ─── Task 2: ViewModel ────────────────────────────────────────────────────────
// StateFlow survives configuration changes (e.g. screen rotation) because the
// ViewModel lifecycle is tied to the ViewModelStore, not the Activity/Composable.
class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UserProfile())
    val uiState: StateFlow<UserProfile> = _uiState.asStateFlow()

    fun updateFromLogin(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun updateFromRegister(fullName: String, username: String, email: String, phone: String) {
        _uiState.update {
            it.copy(fullName = fullName, username = username, email = email, phone = phone)
        }
    }

    fun clear() {
        _uiState.value = UserProfile()
    }
}

// ─── Task 1: Routes (4 screens — safely exceeds the 3-screen minimum) ────────
enum class Screen { LOGIN, REGISTER, HOME, PROFILE }

// ─────────────────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            A216553_PRAAVIEEN_RAJJ_NELSON_LAB04Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigator()
                }
            }
        }
    }

    // ─── Task 1: Navigation ───────────────────────────────────────────────────
    @Composable
    fun AppNavigator(
        navController: NavHostController = rememberNavController(),
        userViewModel: UserViewModel = viewModel()
    ) {
        NavHost(navController = navController, startDestination = Screen.LOGIN.name) {

            composable(route = Screen.LOGIN.name) {
                LoginScreen(
                    onLoginSuccess = { username ->
                        userViewModel.updateFromLogin(username)
                        navController.navigate(Screen.HOME.name)
                    },
                    onGoToRegister = { navController.navigate(Screen.REGISTER.name) }
                )
            }

            composable(route = Screen.REGISTER.name) {
                RegisterScreen(
                    onRegisterSuccess = { fullName, username, email, phone ->
                        userViewModel.updateFromRegister(fullName, username, email, phone)
                        // Pop Register off back stack so Home's back goes to Login, not Register
                        navController.navigate(Screen.HOME.name) {
                            popUpTo(Screen.LOGIN.name) { inclusive = false }
                        }
                    },
                    onGoToLogin = { navController.popBackStack() }
                )
            }

            composable(route = Screen.HOME.name) {
                val uiState by userViewModel.uiState.collectAsState()
                HomeScreen(
                    profile = uiState,
                    onViewProfile = { navController.navigate(Screen.PROFILE.name) },
                    onLogout = {
                        userViewModel.clear()
                        // Remove the entire back stack so Back can never bypass the login screen
                        navController.navigate(Screen.LOGIN.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = Screen.PROFILE.name) {
                val uiState by userViewModel.uiState.collectAsState()
                ProfileScreen(
                    profile = uiState,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    // ─── Screen 1: Login ──────────────────────────────────────────────────────
    @Composable
    fun LoginScreen(onLoginSuccess: (String) -> Unit, onGoToRegister: () -> Unit) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf("") }
        var showSuccess by remember { mutableStateOf(false) }

        // Shake animation on bad login attempt
        val shakeAnim = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        // Wait, then navigate after success banner shows
        LaunchedEffect(showSuccess) {
            if (showSuccess) {
                delay(900)
                onLoginSuccess(username)
            }
        }

        // Slide-in the whole screen on first composition
        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }

        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { it / 4 }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                Text("💰", fontSize = 40.sp)
                Text(
                    "Smart Budget",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text("A216553", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(32.dp))

                // Success banner fades in when login succeeds
                AnimatedVisibility(
                    visible = showSuccess,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            "✅ Login successful! Redirecting...",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Login form card — shakes left/right on invalid submit
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationX = shakeAnim.value },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Login",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it; isError = false },
                            label = { Text("Username") },
                            isError = isError && username.isEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; isError = false },
                            label = { Text("Password") },
                            isError = isError && password.isEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )

                        // Error message fades in
                        AnimatedVisibility(visible = isError) {
                            Text(
                                errorMsg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                when {
                                    username.isEmpty() && password.isEmpty() -> {
                                        errorMsg = "Please enter your username and password."
                                        isError = true
                                        scope.launch {
                                            shakeAnim.animateTo(
                                                targetValue = 0f,
                                                animationSpec = keyframes {
                                                    durationMillis = 400
                                                    -20f at 80 using LinearEasing
                                                    20f at 160 using LinearEasing
                                                    -20f at 240 using LinearEasing
                                                    10f at 320 using LinearEasing
                                                    0f at 400 using LinearEasing
                                                }
                                            )
                                        }
                                    }
                                    username.isEmpty() -> {
                                        errorMsg = "Username cannot be empty."
                                        isError = true
                                        scope.launch {
                                            shakeAnim.animateTo(
                                                targetValue = 0f,
                                                animationSpec = keyframes {
                                                    durationMillis = 300
                                                    -15f at 75 using LinearEasing
                                                    15f at 150 using LinearEasing
                                                    0f at 300 using LinearEasing
                                                }
                                            )
                                        }
                                    }
                                    password.isEmpty() -> {
                                        errorMsg = "Password cannot be empty."
                                        isError = true
                                        scope.launch {
                                            shakeAnim.animateTo(
                                                targetValue = 0f,
                                                animationSpec = keyframes {
                                                    durationMillis = 300
                                                    -15f at 75 using LinearEasing
                                                    15f at 150 using LinearEasing
                                                    0f at 300 using LinearEasing
                                                }
                                            )
                                        }
                                    }
                                    else -> showSuccess = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(if (showSuccess) "Logging in..." else "Login")
                        }
                    }
                }

                TextButton(onClick = onGoToRegister) {
                    Text("Don't have an account? Register")
                }
            }
        }
    }

    // ─── Screen 2: Register ───────────────────────────────────────────────────
    @Composable
    fun RegisterScreen(
        onRegisterSuccess: (String, String, String, String) -> Unit,
        onGoToLogin: () -> Unit
    ) {
        var fullName by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf("") }
        var showSuccess by remember { mutableStateOf(false) }

        LaunchedEffect(showSuccess) {
            if (showSuccess) {
                delay(900)
                onRegisterSuccess(fullName, username, email, phone)
            }
        }

        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }

        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(400)) + slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { it / 4 }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    "Create Account",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text("216553", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(24.dp))

                // Success banner
                AnimatedVisibility(
                    visible = showSuccess,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            "✅ Account created! Redirecting...",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Register",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it; isError = false },
                            label = { Text("Full Name") },
                            isError = isError && fullName.isEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it; isError = false },
                            label = { Text("Username") },
                            isError = isError && username.isEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; isError = false },
                            label = { Text("Email") },
                            isError = isError && email.isEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; isError = false },
                            label = { Text("Password") },
                            isError = isError && password.isEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )

                        AnimatedVisibility(visible = isError) {
                            Text(
                                errorMsg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                when {
                                    fullName.isEmpty() -> { errorMsg = "Full name is required."; isError = true }
                                    username.isEmpty() -> { errorMsg = "Username is required."; isError = true }
                                    email.isEmpty() -> { errorMsg = "Email is required."; isError = true }
                                    password.isEmpty() -> { errorMsg = "Password is required."; isError = true }
                                    else -> showSuccess = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(if (showSuccess) "Creating account..." else "Create Account")
                        }
                    }
                }

                TextButton(onClick = onGoToLogin) {
                    Text("Already have an account? Login")
                }
            }
        }
    }

    // ─── Screen 3: Home ───────────────────────────────────────────────────────
    @Composable
    fun HomeScreen(profile: UserProfile, onViewProfile: () -> Unit, onLogout: () -> Unit) {
        val displayName = when {
            profile.fullName.isNotEmpty() -> profile.fullName
            profile.username.isNotEmpty() -> profile.username
            else -> "User"
        }

        // Full envelope list
        val allEnvelopes = remember {
            listOf(
                Envelope("Food", "🍔", 300f, 120f),
                Envelope("Transport", "🚗", 200f, 80f),
                Envelope("Entertainment", "🎬", 150f, 60f),
                Envelope("Shopping", "🛍️", 250f, 200f),
                Envelope("Health", "💊", 100f, 30f),
                Envelope("Utilities", "💡", 180f, 175f)
            )
        }

        // Search query — filters the envelope list in real time
        var searchQuery by remember { mutableStateOf("") }

        val filteredEnvelopes = remember(searchQuery) {
            if (searchQuery.isEmpty()) allEnvelopes
            else allEnvelopes.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
        }

        // Slide-in animation on home screen load
        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }

        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(400)) + slideInVertically(
                animationSpec = tween(500),
                initialOffsetY = { it / 5 }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "216553 | Smart Budget",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Button(onClick = onLogout) { Text("Logout") }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Balance card — primary color background
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Hello, $displayName 👋",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "RM 850.00",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Available Balance",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile shortcut
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View My Profile →")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Budget Envelopes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Search bar ──────────────────────────────────────────────
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search envelopes...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                            TextButton(onClick = { searchQuery = "" }) {
                                Text("Clear", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Empty state when search finds nothing
                AnimatedVisibility(
                    visible = filteredEnvelopes.isEmpty(),
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    Text(
                        text = "No envelopes match \"$searchQuery\"",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                }

                // ── Filtered envelope cards ─────────────────────────────────
                filteredEnvelopes.forEachIndexed { index, envelope ->
                    ExpandableEnvelopeCard(
                        title = envelope.title,
                        emoji = envelope.emoji,
                        budgetTotal = envelope.budgetTotal,
                        budgetSpent = envelope.budgetSpent
                    )
                    if (index < filteredEnvelopes.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ─── Screen 4: Profile ────────────────────────────────────────────────────
    // Demonstrates ViewModel data persisting and being read on a 4th separate screen
    @Composable
    fun ProfileScreen(profile: UserProfile, onBack: () -> Unit) {
        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }

        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(400)) + slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { it / 4 }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("← Back") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "My Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Avatar circle — first letter of user's name
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(40.dp)
                        )
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.fullName.firstOrNull()?.uppercaseChar()?.toString()
                            ?: profile.username.firstOrNull()?.uppercaseChar()?.toString()
                            ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfileRow(label = "Full Name", value = profile.fullName.ifEmpty { "—" })
                        HorizontalDivider()
                        ProfileRow(label = "Username", value = profile.username.ifEmpty { "—" })
                        HorizontalDivider()
                        ProfileRow(label = "Email", value = profile.email.ifEmpty { "—" })
                        HorizontalDivider()
                        ProfileRow(label = "Phone", value = profile.phone.ifEmpty { "—" })
                    }
                }
            }
        }
    }

    @Composable
    fun ProfileRow(label: String, value: String) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }

    // ─── Reusable: Expandable Envelope Card ───────────────────────────────────
    @Composable
    fun ExpandableEnvelopeCard(
        title: String,
        emoji: String,
        budgetTotal: Float,
        budgetSpent: Float
    ) {
        var expanded by remember { mutableStateOf(false) }
        val budgetLeft = budgetTotal - budgetSpent
        val progress = (budgetSpent / budgetTotal).coerceIn(0f, 1f)

        // animateContentSize with spring bounce on expand/collapse
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .clickable { expanded = !expanded },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {

                // Header row always visible
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(emoji, fontSize = 26.sp)
                        Column {
                            Text(
                                title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "RM %.0f / RM %.0f".format(budgetSpent, budgetTotal),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Expanded detail — AnimatedVisibility fade + slide
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(tween(200)) + slideInVertically(
                        animationSpec = tween(200),
                        initialOffsetY = { -it / 2 }
                    )
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress bar — color shifts based on spending level
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = when {
                                progress >= 0.9f -> MaterialTheme.colorScheme.error
                                progress >= 0.7f -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Spent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    "RM %.2f".format(budgetSpent),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    "RM %.2f".format(budgetLeft),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Warning chip if near or over budget — uses Material errorContainer
                        if (progress >= 0.8f) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (progress >= 1f) "⚠️ Budget exceeded!" else "⚠️ Almost at budget limit!",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}