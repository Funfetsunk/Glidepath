package com.glidepath.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.glidepath.app.domain.model.GoalType
import com.glidepath.app.ui.components.MilestoneOverlay
import com.glidepath.app.ui.goal.GoalCompleteScreen
import com.glidepath.app.ui.goal.GoalScreen
import com.glidepath.app.ui.history.HistoryScreen
import com.glidepath.app.ui.logpayment.LogPaymentScreen
import com.glidepath.app.ui.onboarding.OnboardingScreen
import com.glidepath.app.ui.settings.AppearanceScreen
import com.glidepath.app.ui.settings.SettingsScreen
import com.glidepath.app.ui.theme.GlideTheme
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide

private object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val LOG = "log"
    const val EDIT = "edit"
    const val APPEARANCE = "appearance"
}

/** App root: applies the chosen theme, gates onboarding, and hosts navigation + overlays. */
@Composable
fun GlideApp(viewModel: GlidepathViewModel = hiltViewModel()) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GlideTheme(palette = theme.palette, mode = theme.mode) {
        var milestone by remember { mutableStateOf<Int?>(null) }
        var showComplete by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    is GlideEvent.MilestoneReached -> milestone = event.percent
                    is GlideEvent.GoalCompleted -> showComplete = true
                    is GlideEvent.Toast -> Unit
                }
            }
        }

        val navController = rememberNavController()

        when (val state = uiState) {
            GoalUiState.Loading -> Unit
            GoalUiState.NeedsOnboarding -> OnboardingScreen(
                onFinish = { r ->
                    viewModel.createGoal(r.name, r.type, r.totalPennies, r.currency, r.monthlyTargetPennies)
                },
            )
            is GoalUiState.Ready -> MainScaffold(
                navController = navController,
                state = state,
                theme = theme,
                viewModel = viewModel,
            )
        }

        val readyState = uiState as? GoalUiState.Ready
        val overlayType = readyState?.data?.goal?.type ?: GoalType.DEBT
        milestone?.let { pct ->
            MilestoneOverlay(percent = pct, type = overlayType, onDismiss = { milestone = null })
        }
        if (showComplete && readyState != null) {
            GoalCompleteScreen(
                goal = readyState.data.goal,
                paidPennies = readyState.data.progress.paid,
                paymentCount = readyState.data.payments.size,
                onStartNew = {
                    viewModel.startNewGoal(readyState.data.goal.id)
                    showComplete = false
                },
                onBackToGoal = { showComplete = false },
            )
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    state: GoalUiState.Ready,
    theme: com.glidepath.app.data.local.ThemePrefs,
    viewModel: GlidepathViewModel,
) {
    val glide = LocalGlide.current
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val themeLabel = "${theme.palette.displayName} · ${theme.mode.name.lowercase().replaceFirstChar { it.uppercase() }}"
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBottomBar = route in setOf(Routes.HOME, Routes.HISTORY, Routes.SETTINGS)

    Box(modifier = Modifier.fillMaxSize().background(glide.bg)) {
        NavHost(navController = navController, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                GoalScreen(
                    data = state.data,
                    onLogPayment = { navController.navigate(Routes.LOG) },
                    modifier = Modifier.padding(bottom = 72.dp),
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    data = state.data,
                    onDelete = viewModel::deletePayment,
                    onEdit = viewModel::updatePayment,
                    modifier = Modifier.padding(bottom = 72.dp),
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    goal = state.data.goal,
                    themeLabel = themeLabel,
                    notifications = notifications,
                    onEditGoal = { navController.navigate(Routes.EDIT) },
                    onOpenAppearance = { navController.navigate(Routes.APPEARANCE) },
                    onNotificationsChange = viewModel::setNotifications,
                    onSupport = { /* Play billing — Phase 7 */ },
                    onDeleteGoal = { viewModel.startNewGoal(state.data.goal.id) },
                    modifier = Modifier.padding(bottom = 72.dp),
                )
            }
            composable(Routes.APPEARANCE) {
                AppearanceScreen(
                    palette = theme.palette,
                    mode = theme.mode,
                    onSelect = viewModel::setTheme,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.LOG) {
                LogPaymentScreen(
                    isDebt = state.data.goal.type == GoalType.DEBT,
                    currencyCode = state.data.goal.currency,
                    monthlyTargetPennies = state.data.goal.monthlyTarget,
                    onConfirm = { amount, note ->
                        viewModel.addPayment(state.data.goal.id, amount, System.currentTimeMillis(), note)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() },
                )
            }
            composable(Routes.EDIT) {
                // Edit-goal reuses onboarding steps 2-4 (skips Welcome) and keeps payments (§7.1).
                val goal = state.data.goal
                OnboardingScreen(
                    editMode = true,
                    initial = com.glidepath.app.ui.onboarding.OnboardingResult(
                        name = goal.name,
                        type = goal.type,
                        totalPennies = goal.total,
                        currency = goal.currency,
                        monthlyTargetPennies = goal.monthlyTarget,
                    ),
                    onFinish = { r ->
                        viewModel.updateGoal(
                            goal.copy(
                                name = r.name,
                                type = r.type,
                                total = r.totalPennies,
                                currency = r.currency,
                                monthlyTarget = r.monthlyTargetPennies,
                            ),
                        )
                        navController.popBackStack()
                    },
                )
            }
        }

        if (showBottomBar) {
            BottomBar(
                current = route,
                onSelect = { dest ->
                    navController.navigate(dest) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun BottomBar(current: String?, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val glide = LocalGlide.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(glide.surface)
            .navigationBarsPadding()
            .padding(vertical = 10.dp),
    ) {
        Tab("⌂", "Home", current == Routes.HOME, Modifier.weight(1f)) { onSelect(Routes.HOME) }
        Tab("≡", "History", current == Routes.HISTORY, Modifier.weight(1f)) { onSelect(Routes.HISTORY) }
        Tab("⚙", "Settings", current == Routes.SETTINGS, Modifier.weight(1f)) { onSelect(Routes.SETTINGS) }
    }
}

@Composable
private fun Tab(glyph: String, label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val glide = LocalGlide.current
    val color = if (selected) glide.accent else glide.muted
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(glyph, style = GlideType.body.copy(color = color, textAlign = TextAlign.Center))
        Text(label, style = GlideType.caption.copy(color = color, textAlign = TextAlign.Center))
    }
}
