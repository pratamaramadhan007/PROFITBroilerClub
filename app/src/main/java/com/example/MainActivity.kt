package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.NeonBadge
import com.example.ui.components.QuickCalculatorDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BroilerViewModel
import kotlinx.coroutines.launch

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import com.example.ui.components.AddHarvestTicketDialog
import com.example.ui.components.ClosePeriodDialog
import com.example.ui.components.EditDocChickInDialog
import com.example.ui.components.HarvestReceiptDialog
import com.example.ui.components.LiveScaleDialog
import com.example.ui.components.rememberLiveDateTime

enum class MainNavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
    RECORDING("Recording", Icons.Default.Assignment, "nav_recording"),
    PANEN("Panen", Icons.Default.Scale, "nav_panen"),
    INVENTARIS("Inventaris", Icons.Default.Inventory2, "nav_inventaris"),
    EXPENSES("Biaya", Icons.Default.Paid, "nav_expenses"),
    KOMPARASI("Komparasi", Icons.Default.CompareArrows, "nav_komparasi")
}

class MainActivity : ComponentActivity() {
    private val viewModel: BroilerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.attachStorage(applicationContext)
        enableEdgeToEdge()
        setContent {
            BroilerProTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                if (!state.isLoggedIn) {
                    LoginScreen(state = state, viewModel = viewModel)
                } else {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.persistDataSync()
    }

    override fun onStop() {
        super.onStop()
        viewModel.persistDataSync()
        // Kunci sesi otomatis setiap kali pengguna keluar dari aplikasi
        if (!isChangingConfigurations) {
            viewModel.lockSession()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: BroilerViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var currentNav by remember { mutableStateOf(MainNavDestination.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }
    val liveTime = rememberLiveDateTime()

    // Handle feedback messages (e.g. stock deduction alerts)
    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFeedbackMessage()
        }
    }

    // Android back button returns to Dashboard if on another tab
    BackHandler(enabled = currentNav != MainNavDestination.DASHBOARD) {
        currentNav = MainNavDestination.DASHBOARD
    }

    Scaffold(
        containerColor = CyberDark,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = CyberSurfaceVariant,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Futuristic Robotic Broiler Logo Thumbnail
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberDark)
                                .border(1.5.dp, NeonCyan, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_broiler_cyber_1793847272384),
                                contentDescription = "Logo Broiler Cyber",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PROFIT BROILER",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                NeonBadge(text = "CLUB", color = NeonGreen)
                            }
                            Text(
                                text = "${liveTime.dayName}, ${liveTime.dateFormatted}",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Live Digital Time Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceVariant.copy(alpha = 0.7f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(NeonGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = liveTime.timeFormatted,
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.openCalculatorDialog(true) },
                        modifier = Modifier.testTag("action_calc")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Kalkulator IP",
                            tint = NeonCyan
                        )
                    }

                    // Account Menu: Ubah Password & Logout
                    var showAccountMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showAccountMenu = true },
                            modifier = Modifier.testTag("action_profile")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Akun Profil",
                                tint = NeonGreen
                            )
                        }
                        DropdownMenu(
                            expanded = showAccountMenu,
                            onDismissRequest = { showAccountMenu = false },
                            modifier = Modifier
                                .background(CyberSurface)
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("User: ${state.userId}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Email: ${state.recoveryEmail}", color = TextMuted, fontSize = 10.sp)
                                    }
                                },
                                onClick = {},
                                enabled = false
                            )
                            HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))
                            DropdownMenuItem(
                                text = { Text("Ubah Password (OTP)", color = TextPrimary, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    showAccountMenu = false
                                    viewModel.openChangePasswordDialog(true)
                                },
                                modifier = Modifier.testTag("menu_change_password")
                            )
                            DropdownMenuItem(
                                text = { Text("Keluar (Logout)", color = NeonRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(Icons.Default.Logout, contentDescription = null, tint = NeonRed, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    showAccountMenu = false
                                    viewModel.logout()
                                },
                                modifier = Modifier.testTag("menu_logout")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(
                        1.dp,
                        CyberCardBorder.copy(alpha = 0.6f),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
            ) {
                MainNavDestination.values().forEach { destination ->
                    val isSelected = (currentNav == destination)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentNav = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                tint = if (isSelected) NeonCyan else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                color = if (isSelected) NeonCyan else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan.copy(alpha = 0.15f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentNav,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    MainNavDestination.DASHBOARD -> DashboardScreen(
                        state = state,
                        viewModel = viewModel,
                        onNavigateToRecording = { currentNav = MainNavDestination.RECORDING },
                        onNavigateToPanen = { currentNav = MainNavDestination.PANEN }
                    )
                    MainNavDestination.RECORDING -> RecordingScreen(
                        state = state,
                        viewModel = viewModel
                    )
                    MainNavDestination.PANEN -> HarvestScreen(
                        state = state,
                        viewModel = viewModel
                    )
                    MainNavDestination.INVENTARIS -> InventoryScreen(
                        state = state,
                        viewModel = viewModel
                    )
                    MainNavDestination.EXPENSES -> ExpensesScreen(
                        state = state,
                        viewModel = viewModel
                    )
                    MainNavDestination.KOMPARASI -> ComparisonScreen(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Quick Calculator Modal Dialog
    if (state.isCalculatorDialogOpen) {
        QuickCalculatorDialog(
            onDismiss = { viewModel.openCalculatorDialog(false) }
        )
    }

    // Change Password Dialog (when logged in)
    if (state.isChangePasswordDialogOpen) {
        ResetPasswordOtpDialog(
            state = state,
            onDismiss = { viewModel.openChangePasswordDialog(false) },
            onRequestOtp = { viewModel.requestPasswordResetOtp() },
            onSubmitReset = { otp, newPass ->
                viewModel.verifyOtpAndResetPassword(otp, newPass)
            }
        )
    }

    // Close Period (Panen & Arsip Periode) Dialog
    if (state.isClosePeriodDialogOpen) {
        ClosePeriodDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = { viewModel.openClosePeriodDialog(false) }
        )
    }

    // Input & Edit DOC Masuk (Chick-In) Dialog
    if (state.isEditDocDialogOpen) {
        EditDocChickInDialog(
            currentPeriod = state.currentPeriod,
            onDismiss = { viewModel.openEditDocDialog(false) },
            onSubmit = { startDate, docTime, popL1, popL2, docPrice, strain, hatchery ->
                viewModel.updateDocChickIn(
                    startDate = startDate,
                    docEntryTime = docTime,
                    initialPopulationL1 = popL1,
                    initialPopulationL2 = popL2,
                    docPricePerBird = docPrice,
                    docStrain = strain,
                    docHatchery = hatchery
                )
            }
        )
    }

    // Add New Harvest Ticket Dialog
    if (state.isAddHarvestTicketDialogOpen) {
        AddHarvestTicketDialog(
            state = state,
            viewModel = viewModel,
            onDismiss = { viewModel.openAddHarvestTicketDialog(false) }
        )
    }

    // Interactive Live Scale Logging Dialog
    if (state.isScaleWeighingSheetOpen && state.activeScaleTicketId != null) {
        val activeTicket = state.harvestTickets.find { it.id == state.activeScaleTicketId }
        if (activeTicket != null) {
            LiveScaleDialog(
                ticket = activeTicket,
                viewModel = viewModel,
                onDismiss = { viewModel.openScaleWeighingSheet(null, false) }
            )
        }
    }

    // Digital Receipt & WhatsApp Sharing Dialog
    val receiptTicket = state.selectedTicketForReceipt
    if (state.isReceiptDialogOpen && receiptTicket != null) {
        HarvestReceiptDialog(
            ticket = receiptTicket,
            onDismiss = { viewModel.openReceiptDialog(null, false) }
        )
    }
}

