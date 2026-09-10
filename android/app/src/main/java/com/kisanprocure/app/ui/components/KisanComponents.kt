package com.kisanprocure.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisanprocure.app.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN TOP BAR
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KisanTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = KisanGreenDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = KisanTextMuted
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = KisanGreenDark
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = KisanWhite,
            titleContentColor = KisanGreenDark
        )
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN PRIMARY BUTTON (spring press animation)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = KisanGreenPrimary,
            contentColor = KisanWhite,
            disabledContainerColor = KisanBorder,
            disabledContentColor = KisanTextMuted
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier
            .height(52.dp)
            .scale(scale)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = KisanWhite,
                strokeWidth = 2.5.dp
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN SECONDARY / OUTLINE BUTTON
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "outline_btn_scale"
    )

    OutlinedButton(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = KisanGreenPrimary),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.5.dp
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        modifier = modifier
            .height(48.dp)
            .scale(scale)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN STATUS BADGE
// ═══════════════════════════════════════════════════════════════════════════════

data class StatusStyle(val textColor: Color, val bgColor: Color)

fun getStatusStyle(status: String): StatusStyle = when (status.uppercase()) {
    "OPEN"        -> StatusStyle(KisanStatusOpen, KisanStatusOpenBg)
    "BUSY"        -> StatusStyle(KisanStatusBusy, KisanStatusBusyBg)
    "CLOSED"      -> StatusStyle(KisanStatusClosed, KisanStatusClosedBg)
    "CALLED"      -> StatusStyle(KisanStatusCalled, KisanStatusCalledBg)
    "PROCESSING"  -> StatusStyle(KisanStatusProcessing, KisanStatusProcessingBg)
    "COMPLETED"   -> StatusStyle(KisanStatusCompleted, KisanStatusCompletedBg)
    "WAITING"     -> StatusStyle(KisanStatusWaiting, KisanStatusWaitingBg)
    "CONFIRMED"   -> StatusStyle(KisanStatusOpen, KisanStatusOpenBg)
    "CHECKED_IN"  -> StatusStyle(KisanStatusProcessing, KisanStatusProcessingBg)
    "CANCELLED"   -> StatusStyle(KisanStatusClosed, KisanStatusClosedBg)
    "YOUR TURN"   -> StatusStyle(KisanYourTurn, KisanYourTurnBg)
    else          -> StatusStyle(KisanTextMuted, KisanSurfaceVariant)
}

@Composable
fun KisanStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val style = getStatusStyle(status)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = style.bgColor,
        modifier = modifier
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = style.textColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN METRIC CARD (e.g. "17  In Queue")
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanMetricCard(
    value: String,
    label: String,
    valueColor: Color = KisanGreenPrimary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = valueColor,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = KisanTextMuted,
            textAlign = TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN CARD (standard elevated card)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = KisanWhite),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN SECTION HEADER
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanSectionHeader(
    title: String,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = KisanTextDark
        )
        action?.invoke()
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN EMPTY STATE
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanEmptyState(
    emoji: String,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = KisanTextDark,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = KisanTextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            KisanPrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN ERROR STATE
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚠️", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Connection Error",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = KisanError
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = KisanTextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        KisanPrimaryButton(text = "Retry", onClick = onRetry, icon = Icons.Default.Refresh)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN SKELETON SHIMMER (loading placeholder)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    cornerRadius: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(KisanBorder.copy(alpha = shimmerAlpha))
    )
}

@Composable
fun KisanCardSkeleton(modifier: Modifier = Modifier) {
    KisanCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KisanSkeleton(modifier = Modifier.fillMaxWidth(0.7f), height = 18.dp)
            KisanSkeleton(modifier = Modifier.fillMaxWidth(0.5f), height = 14.dp)
            KisanSkeleton(modifier = Modifier.fillMaxWidth(), height = 12.dp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN CONNECTION STATUS CHIP
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanConnectionChip(isConnected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isConnected) KisanStatusOpenBg else KisanErrorBg,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) KisanStatusOpen else KisanError)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isConnected) "Live" else "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = if (isConnected) KisanStatusOpen else KisanError,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN INFO CHIP (small pill with icon+text, e.g. "4.2 km", "17 in queue")
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanInfoChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = KisanSurfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = KisanTextSecondary)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KISAN STEP INDICATOR
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KisanStepIndicator(
    totalSteps: Int,
    currentStep: Int,
    stepLabels: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stepLabels.forEachIndexed { index, label ->
            val step = index + 1
            val isDone = step < currentStep
            val isActive = step == currentStep
            val isFuture = step > currentStep

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone   -> KisanGreenPrimary
                                isActive -> KisanGreenPrimary
                                else     -> KisanBorder
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = KisanWhite, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = "$step",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isActive) KisanWhite else KisanTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive || isDone) KisanGreenDark else KisanTextMuted,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (index < currentStep - 1) KisanGreenPrimary else KisanBorder)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BOTTOM NAV DEFINITION
// ═══════════════════════════════════════════════════════════════════════════════

sealed class FarmerTab(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home      : FarmerTab("home", "Home", Icons.Default.Home, Icons.Default.Home)
    object Centres   : FarmerTab("centres", "Centres", Icons.Default.Store, Icons.Default.Store)
    object Bookings  : FarmerTab("bookings", "Bookings", Icons.Default.CalendarMonth, Icons.Default.CalendarMonth)
    object Profile   : FarmerTab("profile", "Profile", Icons.Default.Person, Icons.Default.Person)
}

@Composable
fun KisanBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val tabs = listOf(FarmerTab.Home, FarmerTab.Centres, FarmerTab.Bookings, FarmerTab.Profile)

    NavigationBar(
        containerColor = KisanWhite,
        contentColor = KisanGreenPrimary,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(tab.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = KisanGreenPrimary,
                    selectedTextColor = KisanGreenPrimary,
                    indicatorColor = KisanMintContainer,
                    unselectedIconColor = KisanTextMuted,
                    unselectedTextColor = KisanTextMuted
                )
            )
        }
    }
}
