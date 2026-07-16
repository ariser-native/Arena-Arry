package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArenaApp(viewModel: ArenaViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val squadMembers by viewModel.squadMembers.collectAsState()
    val isMicEnabled by viewModel.isMicEnabled.collectAsState()
    val isSpeakerEnabled by viewModel.isSpeakerEnabled.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val matchmakingState by viewModel.matchmakingState.collectAsState()
    val matchmakingTimer by viewModel.matchmakingTimer.collectAsState()
    val challenges by viewModel.challenges.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val esportsPoints by viewModel.esportsPoints.collectAsState()
    val esportsXp by viewModel.esportsXp.collectAsState()
    val weapons by viewModel.weapons.collectAsState()
    val selectedWeapon by viewModel.selectedWeapon.collectAsState()
    val coachResponse by viewModel.coachResponse.collectAsState()
    val isCoachLoading by viewModel.isCoachLoading.collectAsState()

    val diamonds by viewModel.diamonds.collectAsState()
    val shopItems by viewModel.shopItems.collectAsState()
    val unlockedItems by viewModel.unlockedItems.collectAsState()
    val topUpRequests by viewModel.topUpRequests.collectAsState()

    // Auth States
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val loginType by viewModel.loginType.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkStoredLogin(context)
    }

    var showFriendsDrawer by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var showDiamondTopUpDialog by remember { mutableStateOf(false) }
    var showDiamondShopDialog by remember { mutableStateOf(false) }
    var showAdminPortalDialog by remember { mutableStateOf(false) }
    var chatText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val chatScrollState = rememberLazyListState()

    // Auto-scroll chat when message list changes
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatScrollState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    if (!isLoggedIn) {
        FreeFireLoginScreen(
            onLoginComplete = { name, email, type ->
                viewModel.performLogin(name, email, type, context)
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = EsportsDark,
            bottomBar = {
                ArenaBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background mesh/gradient decoration
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                EsportsDark,
                                EsportsSurface,
                                EsportsDark
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Banner with High-Definition generated graphic
                HeaderBanner(
                    esportsPoints = esportsPoints,
                    esportsXp = esportsXp,
                    diamonds = diamonds,
                    currentUser = currentUser,
                    onFriendsToggle = { showFriendsDrawer = !showFriendsDrawer },
                    onTopUpClick = { showTopUpDialog = true },
                    onDiamondTopUpClick = { showDiamondTopUpDialog = true },
                    onOpenShopClick = { showDiamondShopDialog = true },
                    onOpenAdminClick = { showAdminPortalDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Content switching based on active tab
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        ArenaTab.LOBBY -> LobbyTabContent(
                            currentUser = currentUser,
                            squadMembers = squadMembers,
                            isMicEnabled = isMicEnabled,
                            isSpeakerEnabled = isSpeakerEnabled,
                            chatMessages = chatMessages,
                            chatScrollState = chatScrollState,
                            chatText = chatText,
                            matchmakingState = matchmakingState,
                            matchmakingTimer = matchmakingTimer,
                            onChatTextChange = { chatText = it },
                            onSendChat = {
                                viewModel.sendChatMessage(chatText)
                                chatText = ""
                            },
                            onToggleMic = { viewModel.toggleMic() },
                            onToggleSpeaker = { viewModel.toggleSpeaker() },
                            onStartMatch = { viewModel.startMatchmaking() },
                            onCancelMatch = { viewModel.cancelMatchmaking() }
                        )
                        ArenaTab.CHALLENGES -> ChallengesTabContent(
                            challenges = challenges,
                            friends = friends,
                            onClaimReward = { viewModel.claimChallenge(it) }
                        )
                        ArenaTab.WEAPONS -> WeaponsTabContent(
                            weapons = weapons,
                            selectedWeapon = selectedWeapon,
                            onWeaponSelected = { viewModel.selectWeapon(it) }
                        )
                        ArenaTab.COACH -> CoachTabContent(
                            coachResponse = coachResponse,
                            isCoachLoading = isCoachLoading,
                            onAskCoach = { viewModel.askAiCoach(it) }
                        )
                    }
                }
            }

            // Slide-out Friends drawer overlay
            if (showFriendsDrawer) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { showFriendsDrawer = false }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(280.dp)
                            .align(Alignment.CenterEnd)
                            .background(EsportsSurface)
                            .clickable(enabled = false) {}
                            .border(1.dp, CyberTeal.copy(alpha = 0.3f), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    ) {
                        FriendsDrawerContent(
                            friends = friends,
                            squadMembers = squadMembers,
                            onToggleSquadInvite = { viewModel.toggleFriendStatusInLobby(it) },
                            onAddFriend = { viewModel.addMockFriend(it) },
                            onClose = { showFriendsDrawer = false }
                        )
                    }
                }
            }

            if (showTopUpDialog) {
                TopUpDialog(
                    onDismiss = { showTopUpDialog = false },
                    onTopUp = { amount ->
                        viewModel.buyEsportsPoints(amount)
                    }
                )
            }

            if (showDiamondTopUpDialog) {
                DiamondTopUpDialog(
                    onDismiss = { showDiamondTopUpDialog = false },
                    onSubmitRequest = { pkr, diams, trxId, phone ->
                        viewModel.submitTopUpRequest(pkr, diams, trxId, phone)
                        showDiamondTopUpDialog = false
                    }
                )
            }

            if (showDiamondShopDialog) {
                DiamondShopDialog(
                    diamonds = diamonds,
                    shopItems = shopItems,
                    unlockedItems = unlockedItems,
                    onDismiss = { showDiamondShopDialog = false },
                    onBuyItem = { itemId ->
                        viewModel.purchaseShopItem(itemId)
                    }
                )
            }

            if (showAdminPortalDialog) {
                AdminPortalDialog(
                    requests = topUpRequests,
                    squadMembers = squadMembers,
                    friends = friends,
                    currentUser = currentUser,
                    onDismiss = { showAdminPortalDialog = false },
                    onApproveRequest = { viewModel.approveTopUpRequest(it) },
                    onRejectRequest = { viewModel.rejectTopUpRequest(it) },
                    onDirectTransfer = { playerName, amount ->
                        viewModel.directTransferDiamonds(playerName, amount)
                    }
                )
            }
        }
    }
    }
}

// --- Top Header / Banner Component ---
@Composable
fun HeaderBanner(
    esportsPoints: Int,
    esportsXp: Int,
    diamonds: Int,
    currentUser: Player,
    onFriendsToggle: () -> Unit,
    onTopUpClick: () -> Unit,
    onDiamondTopUpClick: () -> Unit,
    onOpenShopClick: () -> Unit,
    onOpenAdminClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
    ) {
        // High-definition cyber banner drawable (generated)
        Image(
            painter = painterResource(id = R.drawable.img_arena_banner_1784131011914),
            contentDescription = "Arena Arry Cyber Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Subtle gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            EsportsDark.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Header content info overlays
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // First Row: Brand Name & Currency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MilitaryTech,
                        contentDescription = "Arena Arry Logo",
                        tint = CyberOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ARENA ARRY",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Esports Points / Coins display
                    Row(
                        modifier = Modifier
                            .clickable { onTopUpClick() }
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(1.dp, CyberOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Esports Coins",
                            tint = CyberOrange,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${esportsPoints} EP",
                            color = TextWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Diamonds display (Click to Top-Up)
                    Row(
                        modifier = Modifier
                            .clickable { onDiamondTopUpClick() }
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(1.2.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp)) // Diamond cyan color
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Diamonds Icon",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$diamonds 💎",
                            color = TextWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Buy Diamonds",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(9.dp)
                        )
                    }

                    // Diamond Shop Button
                    Row(
                        modifier = Modifier
                            .clickable { onOpenShopClick() }
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(1.dp, CyberTeal, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalMall,
                            contentDescription = "Diamond Shop Icon",
                            tint = CyberTeal,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "SHOP",
                            color = CyberTeal,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Admin/Owner Portal Button
                    Row(
                        modifier = Modifier
                            .clickable { onOpenAdminClick() }
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .border(1.2.dp, CyberOrange, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = "Owner Portal Icon",
                            tint = CyberOrange,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "OWNER",
                            color = CyberOrange,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Second Row: Player Profile stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Image (generated avatar)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .border(1.5.dp, CyberOrange, CircleShape)
                            .padding(2.dp)
                            .clip(CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_player_1784131035541),
                            contentDescription = "User Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = currentUser.name,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Lv.${currentUser.level}",
                                color = CyberOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Rank tag
                            Text(
                                text = currentUser.rank,
                                modifier = Modifier
                                    .background(CyberTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(1.dp, CyberTeal, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp),
                                color = CyberTeal,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Friends list button with badge
                IconButton(
                    onClick = onFriendsToggle,
                    modifier = Modifier
                        .size(38.dp)
                        .background(CyberOrange.copy(alpha = 0.2f), CircleShape)
                        .border(1.5.dp, CyberOrange, CircleShape)
                        .testTag("friends_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = "Squad Friends Drawer",
                        tint = CyberOrange
                    )
                }
            }
        }
    }
}

// --- Bottom Bar Navigation ---
@Composable
fun ArenaBottomBar(
    currentTab: ArenaTab,
    onTabSelected: (ArenaTab) -> Unit
) {
    NavigationBar(
        containerColor = EsportsSurface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .border(1.dp, CyberOrange.copy(alpha = 0.3f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = currentTab == ArenaTab.LOBBY,
            onClick = { onTabSelected(ArenaTab.LOBBY) },
            icon = { Icon(Icons.Filled.Group, contentDescription = "Lobby") },
            label = { Text("LOBBY", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyberOrange,
                selectedTextColor = CyberOrange,
                indicatorColor = CyberOrange.copy(alpha = 0.15f),
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            ),
            modifier = Modifier.testTag("nav_lobby")
        )
        NavigationBarItem(
            selected = currentTab == ArenaTab.CHALLENGES,
            onClick = { onTabSelected(ArenaTab.CHALLENGES) },
            icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Challenges") },
            label = { Text("MISSIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyberTeal,
                selectedTextColor = CyberTeal,
                indicatorColor = CyberTeal.copy(alpha = 0.15f),
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            ),
            modifier = Modifier.testTag("nav_missions")
        )
        NavigationBarItem(
            selected = currentTab == ArenaTab.WEAPONS,
            onClick = { onTabSelected(ArenaTab.WEAPONS) },
            icon = { Icon(Icons.Filled.Star, contentDescription = "Weapons") },
            label = { Text("LOADOUT", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyberOrange,
                selectedTextColor = CyberOrange,
                indicatorColor = CyberOrange.copy(alpha = 0.15f),
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            ),
            modifier = Modifier.testTag("nav_loadout")
        )
        NavigationBarItem(
            selected = currentTab == ArenaTab.COACH,
            onClick = { onTabSelected(ArenaTab.COACH) },
            icon = { Icon(Icons.Filled.SmartToy, contentDescription = "Coach") },
            label = { Text("AI COACH", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyberTeal,
                selectedTextColor = CyberTeal,
                indicatorColor = CyberTeal.copy(alpha = 0.15f),
                unselectedIconColor = TextGray,
                unselectedTextColor = TextGray
            ),
            modifier = Modifier.testTag("nav_coach")
        )
    }
}

// ==========================================
// TAB 1: SQUAD LOBBY CONTENT
// ==========================================
@Composable
fun LobbyTabContent(
    currentUser: Player,
    squadMembers: List<Player>,
    isMicEnabled: Boolean,
    isSpeakerEnabled: Boolean,
    chatMessages: List<ChatMessage>,
    chatScrollState: androidx.compose.foundation.lazy.LazyListState,
    chatText: String,
    matchmakingState: MatchmakingStatus,
    matchmakingTimer: Int,
    onChatTextChange: (String) -> Unit,
    onSendChat: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onStartMatch: () -> Unit,
    onCancelMatch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        // Tactical Squad Display title with mic indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SQUAD LOBBY",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Active team slots for online matchmaking",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }

            // Audio Communication status controls (reminiscent of Free Fire mic)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onToggleMic,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isMicEnabled) CyberOrange.copy(alpha = 0.15f) else EsportsCard,
                            CircleShape
                        )
                        .border(1.dp, if (isMicEnabled) CyberOrange else Color.Gray, CircleShape),
                    colors = IconButtonDefaults.iconButtonColors()
                ) {
                    Icon(
                        imageVector = if (isMicEnabled) Icons.Filled.Mic else Icons.Filled.MicOff,
                        contentDescription = "Mic Settings",
                        tint = if (isMicEnabled) CyberOrange else TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleSpeaker,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isSpeakerEnabled) CyberTeal.copy(alpha = 0.15f) else EsportsCard,
                            CircleShape
                        )
                        .border(1.dp, if (isSpeakerEnabled) CyberTeal else Color.Gray, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isSpeakerEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                        contentDescription = "Speaker Settings",
                        tint = if (isSpeakerEnabled) CyberTeal else TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lobby Grid representing the 4 player squad slots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Slot 1: Current User
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.8f)
            ) {
                SquadMemberCard(player = currentUser, isCurrentUser = true)
            }

            // Other Slots
            for (i in 0..2) {
                val member = squadMembers.getOrNull(i)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.8f)
                ) {
                    if (member != null) {
                        SquadMemberCard(player = member, isCurrentUser = false)
                    } else {
                        EmptySquadSlot()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MATCHMAKING STATUS ACTION HUB ---
        MatchmakingHubWidget(
            matchmakingState = matchmakingState,
            timerSeconds = matchmakingTimer,
            onStartMatch = onStartMatch,
            onCancelMatch = onCancelMatch
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- SQUAD CHAT CONSOLE PANEL ---
        SquadChatConsole(
            messages = chatMessages,
            scrollState = chatScrollState,
            text = chatText,
            onTextChange = onChatTextChange,
            onSend = onSendChat
        )
    }
}

@Composable
fun SquadMemberCard(player: Player, isCurrentUser: Boolean) {
    var bounceState by remember { mutableStateOf(false) }
    
    // Wave ripple animation for speaking teammate
    val infiniteTransition = rememberInfiniteTransition(label = "voiceRipple")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (player.isSpeaking) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voiceScale"
    )

    Card(
        modifier = Modifier
            .fillMaxSize()
            .border(
                1.5.dp,
                if (isCurrentUser) CyberOrange else CyberTeal.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = EsportsCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Speaking indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lv.${player.level}",
                    color = if (isCurrentUser) CyberOrange else CyberTeal,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )

                if (player.isSpeaking) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                }
            }

            // Avatar placeholder styled
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(
                        1.5.dp,
                        if (player.isSpeaking) NeonGreen else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = player.name,
                    tint = if (isCurrentUser) CyberOrange else CyberTeal,
                    modifier = Modifier.size(28.dp)
                )

                // Render voice wave lines
                if (player.isSpeaking) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = NeonGreen.copy(alpha = 0.3f),
                            radius = size.minDimension / 2 * scale,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            // Nickname & Gun Loadout info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = player.name,
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = player.selectedWeaponName,
                    color = if (isCurrentUser) CyberOrangeLight else CyberTeal.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun EmptySquadSlot() {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = EsportsSurface.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Invite Squad Slot",
                    tint = TextGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "INVITE",
                    color = TextGray.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- MATCHMAKING WIDGET COMPONENT ---
@Composable
fun MatchmakingHubWidget(
    matchmakingState: MatchmakingStatus,
    timerSeconds: Int,
    onStartMatch: () -> Unit,
    onCancelMatch: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberOrange.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = EsportsSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (matchmakingState) {
                is MatchmakingStatus.Idle -> {
                    Text(
                        text = "MULTIPLAYER MATCHMAKING",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Queue up for online esports custom servers",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onStartMatch,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                            .testTag("start_matchmaking_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Start Queue",
                            tint = TextWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START QUEUE",
                            color = TextWhite,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
                is MatchmakingStatus.Finding -> {
                    val minutes = timerSeconds / 60
                    val seconds = timerSeconds % 60
                    val timeStr = String.format("%02d:%02d", minutes, seconds)

                    // Flashing glowing loader
                    val infiniteTransition = rememberInfiniteTransition(label = "findingLoader")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(CyberOrange.copy(alpha = alpha))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FINDING MATCH ONLINE",
                            color = CyberOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Time Elapsed: $timeStr | Server Latency: 42ms",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onCancelMatch,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(42.dp)
                            .testTag("cancel_matchmaking_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EsportsCard),
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "CANCEL",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                is MatchmakingStatus.Found -> {
                    Text(
                        text = "💥 MATCH FOUND!",
                        color = NeonGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Entering custom tactical channel...",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        color = NeonGreen,
                        trackColor = EsportsCard,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
                is MatchmakingStatus.Starting -> {
                    Text(
                        text = "TACTICAL LAUNCH",
                        color = CyberTeal,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Display massive countdown
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(CyberTeal.copy(alpha = 0.15f), CircleShape)
                            .border(2.dp, CyberTeal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(matchmakingState as MatchmakingStatus.Starting).countdown}",
                            color = CyberTeal,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Prepare to drop on battlefield!",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
                is MatchmakingStatus.InGame -> {
                    Text(
                        text = "⚔️ MATCH IS LIVE!",
                        color = CyberOrange,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Arry's Squad drops in 3, 2, 1... Booyah!",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Simulating online tactics. Wait for results...",
                        color = TextGray,
                        fontSize = 11.sp,
                        style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                }
            }
        }
    }
}

// --- CHAT CONSOLE COMPONENT ---
@Composable
fun SquadChatConsole(
    messages: List<ChatMessage>,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .border(1.dp, CyberTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = EsportsSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Chat header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Squad Chat",
                        tint = CyberTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TEAM CONSOLE",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "ONLINE SQUAD CHANNEL",
                    color = NeonGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chat Messages Column
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                items(messages) { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        val senderColor = when {
                            msg.isSystem -> Color.Yellow
                            msg.isCoach -> CyberTeal
                            msg.isCurrentUser -> CyberOrange
                            else -> CyberOrangeLight
                        }

                        val senderPrefix = when {
                            msg.isSystem -> "[SYSTEM]"
                            msg.isCoach -> "[COACH ARRY]"
                            else -> msg.sender
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = senderPrefix,
                                    color = senderColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (msg.timestamp.isNotEmpty()) {
                                    Text(
                                        text = msg.timestamp,
                                        color = TextGray,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Text(
                                text = msg.message,
                                color = if (msg.isSystem) Color.Yellow.copy(alpha = 0.85f) else TextWhite,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("chat_input_field"),
                    placeholder = { Text("Ask Coach or chat with squad...", color = TextGray, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = CyberTeal,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = EsportsCard,
                        unfocusedContainerColor = EsportsCard
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 1,
                    singleLine = true
                )

                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(44.dp)
                        .background(CyberTeal, RoundedCornerShape(10.dp))
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send squad text",
                        tint = EsportsDark
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 2: CHALLENGES & GLOBAL ARENA LEADERBOARD
// ==========================================
@Composable
fun ChallengesTabContent(
    challenges: List<Challenge>,
    friends: List<Friend>,
    onClaimReward: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Text(
            text = "DAILY ESCAPE MISSIONS",
            color = TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Complete tactical multiplayer tasks for arena points",
            color = TextGray,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Missions List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            challenges.forEach { mission ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (mission.isCompleted) NeonGreen.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = EsportsSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = mission.title,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mission.gameMode,
                                    color = CyberTeal,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(CyberTeal.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mission.description,
                                color = TextGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Progress bar
                            val progressPercent = mission.progress.toFloat() / mission.maxProgress.toFloat()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { progressPercent },
                                    color = if (mission.isCompleted) NeonGreen else CyberOrange,
                                    trackColor = EsportsCard,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${mission.progress}/${mission.maxProgress}",
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Claim or Completed Button
                        if (mission.isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Completed",
                                tint = NeonGreen,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(NeonGreen.copy(alpha = 0.15f), CircleShape)
                                    .padding(6.dp)
                            )
                        } else if (mission.progress >= mission.maxProgress) {
                            Button(
                                onClick = { onClaimReward(mission.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("claim_button_${mission.id}")
                            ) {
                                Text("CLAIM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "+${mission.rewardPoints} EP",
                                    color = CyberOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+${mission.xpReward} XP",
                                    color = TextGray,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- GLOBAL MULTIPLAYER LEADERBOARD ---
        Text(
            text = "GLOBAL CHALLENGERS",
            color = TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Top online players across the global servers",
            color = TextGray,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = EsportsSurface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Leaders
                val mockLeaders = listOf(
                    Triple("Alpha_Rush", "Grandmaster I", 9500),
                    Triple("Viper_FF", "Grandmaster II", 8940),
                    Triple("Sniper_God", "Grandmaster III", 8420),
                    Triple("Arry_Pro_Sniper", "Heroic (Aap)", 2450)
                )

                mockLeaders.forEachIndexed { idx, player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (player.first.contains("Arry")) CyberOrange.copy(alpha = 0.15f)
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${idx + 1}",
                                color = if (idx == 0) CyberOrange else if (idx == 1) CyberTeal else TextWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.width(32.dp)
                            )

                            Column {
                                Text(
                                    text = player.first,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = player.second,
                                    color = TextGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "${player.third} Points",
                            color = if (idx == 0) CyberOrange else CyberTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (idx < mockLeaders.size - 1) {
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: WEAPONS LOADOUT CONTENT
// ==========================================
@Composable
fun WeaponsTabContent(
    weapons: List<Weapon>,
    selectedWeapon: Weapon?,
    onWeaponSelected: (Weapon) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Text(
            text = "TACTICAL ARMORY",
            color = TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Select weapon to equip as active loadout in the squad",
            color = TextGray,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Weapon Details Preview Card
        if (selectedWeapon != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, CyberOrange, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = EsportsSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Gun Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedWeapon.name,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedWeapon.type,
                                    color = CyberTeal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(CyberTeal.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedWeapon.description,
                                color = TextGray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        // Tier Tag
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(CyberOrange.copy(alpha = 0.2f), CircleShape)
                                .border(1.5.dp, CyberOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedWeapon.tier,
                                color = CyberOrange,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Gun Stats
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        WeaponStatRow("DAMAGE", selectedWeapon.damage, CyberOrange)
                        WeaponStatRow("FIRE RATE", selectedWeapon.fireRate, CyberTeal)
                        WeaponStatRow("ACCURACY", selectedWeapon.accuracy, NeonGreen)
                        WeaponStatRow("RANGE", selectedWeapon.range, Color.Yellow)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "EQUIPPED FOR ARENA SQUAD",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeonGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Horizontal Grid/List of Weapons to Equip
        Text(
            text = "AVAILABLE ARMORY LOADOUT",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            weapons.forEach { weapon ->
                val isSelected = selectedWeapon?.name == weapon.name
                Card(
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { onWeaponSelected(weapon) }
                        .border(
                            1.dp,
                            if (isSelected) CyberOrange else Color.Gray.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) EsportsCard else EsportsSurface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MilitaryTech,
                            contentDescription = weapon.name,
                            tint = if (isSelected) CyberOrange else TextGray,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = weapon.name,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = weapon.type,
                            color = TextGray,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeaponStatRow(label: String, value: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = "$value", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { value.toFloat() / 100f },
            color = color,
            trackColor = EsportsCard,
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp))
        )
    }
}

// ==========================================
// TAB 4: TACTICAL COACH ARRY (AI GEMINI)
// ==========================================
@Composable
fun CoachTabContent(
    coachResponse: String,
    isCoachLoading: Boolean,
    onAskCoach: (String) -> Unit
) {
    var customQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        // Coach Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.SmartToy,
                contentDescription = "Tactical AI Coach",
                tint = CyberTeal,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ARRY TACTICAL COACH",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Powered by Gemini Server-Side AI Pro",
                    color = CyberTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Response Speech Bubble Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, CyberTeal.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = EsportsSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COACH WISDOM (Hinglish/Roman Urdu)",
                        color = CyberTeal,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                    
                    if (isCoachLoading) {
                        CircularProgressIndicator(
                            color = CyberTeal,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "STATUS: SYNCD",
                            color = NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = coachResponse,
                        color = TextWhite,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Preset Suggestions Row (Free Fire tactical queries)
        Text(
            text = "QUICK TACTICAL PRESETS",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        val presets = listOf(
            "Best Close Range Rush Weapons?",
            "Gloo Wall Protection Strategy",
            "Heroic Rank push guide Hinglish",
            "AWM headshot drop location guide"
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAskCoach(preset) }
                        .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = EsportsCard)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MilitaryTech,
                            contentDescription = "Tactical Query",
                            tint = CyberTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = preset,
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Query input block
        Text(
            text = "ASK CUSTOM GAME STRATEGY",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = customQuery,
                onValueChange = { customQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("coach_custom_input"),
                placeholder = { Text("e.g. Alok vs Chrono skill guide...", color = TextGray, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = CyberTeal,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = EsportsCard,
                    unfocusedContainerColor = EsportsCard
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                shape = RoundedCornerShape(10.dp),
                maxLines = 1,
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (customQuery.isNotBlank()) {
                        onAskCoach(customQuery)
                        customQuery = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(CyberTeal, RoundedCornerShape(10.dp))
                    .testTag("send_coach_query")
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Ask Arry Coach",
                    tint = EsportsDark
                )
            }
        }
    }
}

// ==========================================
// FRIENDS DRAWER COLUMNS
// ==========================================
@Composable
fun FriendsDrawerContent(
    friends: List<Friend>,
    squadMembers: List<Player>,
    onToggleSquadInvite: (Friend) -> Unit,
    onAddFriend: (String) -> Unit,
    onClose: () -> Unit
) {
    var searchFriendName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ONLINE SQUADMATES",
                color = TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 1.sp
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close Friends Drawer",
                    tint = TextWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Add Friend Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = searchFriendName,
                onValueChange = { searchFriendName = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("add_friend_field"),
                placeholder = { Text("Search/Add Friend...", color = TextGray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = CyberOrange,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = EsportsDark,
                    unfocusedContainerColor = EsportsDark
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                shape = RoundedCornerShape(8.dp),
                maxLines = 1,
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (searchFriendName.isNotBlank()) {
                        onAddFriend(searchFriendName)
                        searchFriendName = ""
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(CyberOrange, RoundedCornerShape(8.dp))
                    .testTag("add_friend_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Friend Button",
                    tint = TextWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Friends Scroll list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends) { friend ->
                val isInSquad = squadMembers.any { it.name == friend.name }
                val statusColor = when (friend.status) {
                    FriendStatus.ONLINE -> NeonGreen
                    FriendStatus.IN_LOBBY -> CyberTeal
                    FriendStatus.IN_MATCH -> CyberOrange
                    FriendStatus.OFFLINE -> Color.Gray
                }

                val statusLabel = when (friend.status) {
                    FriendStatus.ONLINE -> "Online"
                    FriendStatus.IN_LOBBY -> "In Lobby"
                    FriendStatus.IN_MATCH -> "In Match"
                    FriendStatus.OFFLINE -> "Offline"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EsportsCard)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(statusColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = friend.name,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lv.${friend.level} | ${friend.rank}",
                                color = TextGray,
                                fontSize = 10.sp
                            )
                            Text(
                                text = statusLabel,
                                color = statusColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Invite/Kick button based on squad presence
                        if (friend.status != FriendStatus.OFFLINE && friend.status != FriendStatus.IN_MATCH) {
                            Button(
                                onClick = { onToggleSquadInvite(friend) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isInSquad) Color.Red else CyberTeal
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("friend_invite_button_${friend.name}")
                            ) {
                                Text(
                                    text = if (isInSquad) "KICK" else "INVITE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopUpDialog(
    onDismiss: () -> Unit,
    onTopUp: (Int) -> Unit
) {
    var selectedPackIndex by remember { mutableStateOf(1) }
    var selectedPaymentIndex by remember { mutableStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val packs = listOf(
        TopUpPack(100, 10, "Rs. 150"),
        TopUpPack(310, 35, "Rs. 450"),
        TopUpPack(520, 60, "Rs. 750"),
        TopUpPack(1060, 150, "Rs. 1500"),
        TopUpPack(2180, 350, "Rs. 3000")
    )
    
    val paymentMethods = listOf(
        PaymentMethod("EasyPaisa", Icons.Filled.AccountBalanceWallet, Color(0xFF00FF66)),
        PaymentMethod("JazzCash", Icons.Filled.Payments, CyberOrange),
        PaymentMethod("Google Play Billing", Icons.Filled.CreditCard, CyberTeal)
    )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .border(2.dp, CyberOrange, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = EsportsSurface
        ) {
            if (isSuccess) {
                val pack = packs[selectedPackIndex]
                val totalEp = pack.amount + pack.bonus
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(CyberTeal.copy(alpha = 0.2f), CircleShape)
                            .border(3.dp, CyberTeal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = CyberTeal,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "TOP-UP SUCCESSFUL!",
                        color = CyberTeal,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "+${totalEp} EP has been credited to your account.",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "(Base: ${pack.amount} EP + Bonus: ${pack.bonus} EP)",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            onTopUp(totalEp)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(180.dp)
                            .height(44.dp)
                    ) {
                        Text(
                            text = "BOOYAH!",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (isProcessing) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = CyberOrange)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Processing Secure Payment...",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Please do not close the app or go back.",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocalMall,
                                contentDescription = "Shop icon",
                                tint = CyberOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EP TOP-UP CENTER",
                                color = TextWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Dialog",
                                tint = TextWhite
                            )
                        }
                    }
                    
                    Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Pane: Pack selector list
                        Column(
                            modifier = Modifier
                                .weight(1.3f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            packs.forEachIndexed { index, pack ->
                                val isSelected = selectedPackIndex == index
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) CyberOrange.copy(alpha = 0.15f) else EsportsDark.copy(alpha = 0.6f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) CyberOrange else Color.Gray.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedPackIndex = index }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Diamonds",
                                            tint = CyberTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "${pack.amount} EP",
                                                color = TextWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            if (pack.bonus > 0) {
                                                Text(
                                                    text = "+${pack.bonus} EP BONUS",
                                                    color = CyberTeal,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                    
                                    Text(
                                        text = pack.price,
                                        color = CyberOrange,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        
                        // Right Pane: Payment selector and buy summary
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "SELECT PAYMENT METHOD",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                paymentMethods.forEachIndexed { index, method ->
                                    val isSelected = selectedPaymentIndex == index
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isSelected) method.accentColor.copy(alpha = 0.15f) else EsportsDark.copy(alpha = 0.6f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) method.accentColor else Color.Gray.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedPaymentIndex = index }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = method.icon,
                                            contentDescription = method.name,
                                            tint = if (isSelected) method.accentColor else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = method.name,
                                            color = if (isSelected) TextWhite else TextGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            
                            // Bottom Summary and Button
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val selectedPack = packs[selectedPackIndex]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Selected Item:", color = TextGray, fontSize = 11.sp)
                                    Text("${selectedPack.amount + selectedPack.bonus} EP", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Payment Via:", color = TextGray, fontSize = 11.sp)
                                    Text(paymentMethods[selectedPaymentIndex].name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                
                                Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Amount:", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(selectedPack.price, color = CyberOrange, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                }
                                
                                Button(
                                    onClick = {
                                        isProcessing = true
                                        scope.launch {
                                            delay(1500)
                                            isProcessing = false
                                            isSuccess = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    Text(
                                        text = "CONFIRM TOP-UP",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TopUpPack(val amount: Int, val bonus: Int, val price: String)
data class PaymentMethod(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val accentColor: Color)

// --- EasyPaisa Diamond Top-Up Dialog (With Urdu Instructions) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiamondTopUpDialog(
    onDismiss: () -> Unit,
    onSubmitRequest: (pkr: Int, diamonds: Int, trxId: String, phone: String) -> Unit
) {
    var selectedPackIndex by remember { mutableStateOf(2) } // default 300 PKR pack
    var trxId by remember { mutableStateOf("") }
    var senderPhone by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val diamondPacks = listOf(
        TopUpPack(42, 0, "Rs. 50"),
        TopUpPack(85, 0, "Rs. 100"),
        TopUpPack(255, 0, "Rs. 300"),
        TopUpPack(430, 0, "Rs. 500"),
        TopUpPack(875, 0, "Rs. 1000"),
        TopUpPack(8750, 0, "Rs. 10000"),
        TopUpPack(17600, 0, "Rs. 20000")
    )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = EsportsSurface
        ) {
            if (isSuccess) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f), CircleShape)
                            .border(3.dp, Color(0xFF00E5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💎", fontSize = 48.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "درخواست کامیابی سے جمع ہو گئی!",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "آپ کا ایزی پیسہ ٹرانزیکشن ثبوت بھیج دیا گیا ہے۔ ایڈمن (Ansa Tasnim) تصدیق کر کے جلد ہی ڈائمنڈز آپ کے اکاؤنٹ میں منتقل کر دیں گے۔",
                        color = TextWhite,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(160.dp)
                    ) {
                        Text("ٹھیک ہے (OK)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💎", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ایزی پیسہ ڈائمنڈز ٹاپ اپ سینٹر",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = TextWhite)
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Main Content Split-Pane
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Column: Diamond Packs Selector List
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ڈائمنڈ پیکج منتخب کریں (SELECT PACK)",
                                color = TextGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )

                            diamondPacks.forEachIndexed { index, pack ->
                                val isSelected = selectedPackIndex == index
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f) else EsportsDark.copy(alpha = 0.6f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF00E5FF) else Color.Gray.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedPackIndex = index }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💎", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${pack.amount} Diamonds",
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Text(
                                        text = pack.price,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Right Column: Payment Details and Form Submission
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // EasyPaisa credentials box
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF004D40)),
                                    border = BorderStroke(1.dp, Color(0xFF00FF66)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "🟢 EASYPAISA PAYMENT DETAILS",
                                            color = Color(0xFF00FF66),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "اس اکاؤنٹ پر ایزی پیسہ کریں:",
                                            color = TextWhite,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "ACCOUNT NAME: Ansa Tasnim",
                                            color = TextWhite,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "NUMBER: 03464410067",
                                            color = Color(0xFF00FF66),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Text(
                                    text = "ادائیگی کا ثبوت (SUBMIT PROOF)",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = senderPhone,
                                    onValueChange = { senderPhone = it },
                                    label = { Text("اپنا موبائل نمبر (Sender Phone)", color = TextGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedBorderColor = Color(0xFF00E5FF),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = trxId,
                                    onValueChange = { trxId = it },
                                    label = { Text("ٹرانزیکشن آئی ڈی / TRX ID", color = TextGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedBorderColor = Color(0xFF00E5FF),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Payment Summary and Submit
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val selectedPack = diamondPacks[selectedPackIndex]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("کل قیمت:", color = TextGray, fontSize = 11.sp)
                                    Text(selectedPack.price, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("ڈائمنڈز حاصل ہوں گے:", color = TextGray, fontSize = 11.sp)
                                    Text("${selectedPack.amount} 💎", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        if (senderPhone.isBlank() || trxId.isBlank()) return@Button
                                        isSubmitting = true
                                        scope.launch {
                                            delay(1500)
                                            isSubmitting = false
                                            onSubmitRequest(
                                                selectedPack.price.replace("Rs. ", "").trim().toInt(),
                                                selectedPack.amount,
                                                trxId,
                                                senderPhone
                                            )
                                            isSuccess = true
                                        }
                                    },
                                    enabled = senderPhone.isNotBlank() && trxId.isNotBlank() && !isSubmitting,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(
                                            text = "تصدیق کی درخواست بھیجیں",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
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

// --- Diamond Shop Dialog (Free Fire Bundles & Emotes) ---
@Composable
fun DiamondShopDialog(
    diamonds: Int,
    shopItems: List<ShopItem>,
    unlockedItems: Set<String>,
    onDismiss: () -> Unit,
    onBuyItem: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") } // "ALL", "BUNDLE", "EMOTE"
    var successUnlockedItem by remember { mutableStateOf<ShopItem?>(null) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .border(2.dp, Color(0xFF9D00FF), RoundedCornerShape(16.dp)), // Purple Theme for Shop
            shape = RoundedCornerShape(16.dp),
            color = EsportsSurface
        ) {
            if (successUnlockedItem != null) {
                // Congratulations Dialog Box inside Shop
                val item = successUnlockedItem!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .background(EsportsDark.copy(alpha = 0.9f)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎉 BOOYAH! UNLOCKED 🎉",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .border(2.dp, Color(0xFFFFD700), CircleShape)
                            .padding(8.dp)
                            .background(Color(0xFF9D00FF).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (item.category == "Bundle") "👕" else "🕺", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.rarity,
                                color = if (item.rarity == "Legendary") Color(0xFFFFD700) else Color(0xFFA020F0),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = item.name,
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )

                    Text(
                        text = item.description,
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { successUnlockedItem = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(180.dp)
                    ) {
                        Text("EQUIP (لسٹ میں شامل کریں)", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Bar with Diamond Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛒", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "کاسمک ڈائمنڈ شاپ (DIAMOND STORE)",
                                color = Color(0xFF9D00FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        // Diamond Balance
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .border(1.2.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("💎", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$diamonds Diamonds",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = TextWhite)
                        }
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filters Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("ALL" to "سب دیکھیں", "BUNDLE" to "بنڈلز (Bundles)", "EMOTE" to "ایموٹس (Emotes)").forEach { (cat, desc) ->
                            val isSelected = selectedCategory == cat
                            Button(
                                onClick = { selectedCategory = cat },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF9D00FF) else EsportsDark.copy(alpha = 0.6f)
                                ),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF9D00FF) else Color.Gray.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$desc",
                                    color = if (isSelected) Color.White else TextGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Store Items List (Scrollable grid/list layout)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val filteredItems = shopItems.filter {
                            selectedCategory == "ALL" || it.category.uppercase() == selectedCategory
                        }

                        // Display in 2 items per row
                        for (i in filteredItems.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val item1 = filteredItems[i]
                                val item2 = if (i + 1 < filteredItems.size) filteredItems[i + 1] else null

                                StoreItemCard(
                                    item = item1,
                                    isUnlocked = unlockedItems.contains(item1.id),
                                    canAfford = diamonds >= item1.price,
                                    modifier = Modifier.weight(1f),
                                    onBuy = {
                                        onBuyItem(item1.id)
                                        if (diamonds >= item1.price) {
                                            successUnlockedItem = item1
                                        }
                                    }
                                )

                                if (item2 != null) {
                                    StoreItemCard(
                                        item = item2,
                                        isUnlocked = unlockedItems.contains(item2.id),
                                        canAfford = diamonds >= item2.price,
                                        modifier = Modifier.weight(1f),
                                        onBuy = {
                                            onBuyItem(item2.id)
                                            if (diamonds >= item2.price) {
                                                successUnlockedItem = item2
                                            }
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreItemCard(
    item: ShopItem,
    isUnlocked: Boolean,
    canAfford: Boolean,
    modifier: Modifier = Modifier,
    onBuy: () -> Unit
) {
    val rarityColor = when (item.rarity) {
        "Legendary" -> Color(0xFFFFD700)
        "Epic" -> Color(0xFFC084FC)
        else -> Color(0xFF38BDF8)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = EsportsDark.copy(alpha = 0.5f)),
        border = BorderStroke(1.2.dp, if (isUnlocked) Color(0xFF00FF66) else rarityColor.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon emoji
                Text(
                    text = if (item.category == "Bundle") "👕 بنڈل" else "🕺 ایموٹ",
                    color = TextGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                // Rarity Tag
                Text(
                    text = item.rarity,
                    color = rarityColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .background(rarityColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }

            Text(
                text = item.name,
                color = TextWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )

            Text(
                text = item.description,
                color = TextGray,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 12.sp
            )

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price Display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💎", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${item.price}",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                // Buy / Equip Button
                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF004D20), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF00FF66), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "UNLOCKED ✅",
                            color = Color(0xFF00FF66),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onBuy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) Color(0xFF9D00FF) else Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "BUY (خریدیں)",
                            color = if (canAfford) Color.White else TextGray,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

// --- Admin Portal Dialog (Separate Dashboard for Owner Ansa Tasnim to credit players) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalDialog(
    requests: List<TopUpRequest>,
    squadMembers: List<Player>,
    friends: List<Friend>,
    currentUser: Player,
    onDismiss: () -> Unit,
    onApproveRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onDirectTransfer: (playerName: String, amount: Int) -> Unit
) {
    var adminSection by remember { mutableStateOf("EASYPAISA_VERIFY") } // "EASYPAISA_VERIFY" or "DIRECT_INJECT"
    var selectedPlayerName by remember { mutableStateOf(currentUser.name) }
    var directInjectAmountStr by remember { mutableStateOf("1000") }
    var directStatusMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Aggregate unique players list for injection
    val allActivePlayers = remember {
        val list = mutableSetOf(currentUser.name)
        squadMembers.forEach { list.add(it.name) }
        friends.forEach { list.add(it.name) }
        list.toList()
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .border(2.dp, CyberOrange, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = EsportsSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header console bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔑", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مالک ایڈمن پورٹل (OWNER CONTROL HUB)",
                            color = CyberOrange,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    // EasyPaisa Owner Tag
                    Text(
                        text = "ACCOUNT: Ansa Tasnim (03464410067)",
                        color = Color(0xFF00FF66),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color(0xFF004D20), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = TextWhite)
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
                
                // Admin Identity Card with anasokie2143@gmail.com
                Card(
                    colors = CardDefaults.cardColors(containerColor = EsportsDark),
                    border = BorderStroke(1.2.dp, CyberOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "👑 OWNER ADMIN CONFIGURATION",
                                color = CyberOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📧 Email: ", color = TextGray, fontSize = 11.sp)
                                Text("anasokie2143@gmail.com", color = TextWhite, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💼 EasyPaisa Name: ", color = TextGray, fontSize = 11.sp)
                                Text("Ansa Tasnim", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        val localContext = LocalContext.current
                        Button(
                            onClick = {
                                val clipboard = localContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Admin Email", "anasokie2143@gmail.com")
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(localContext, "Admin email copied!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy Email",
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کاپی کریں", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section Navigation Tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { adminSection = "EASYPAISA_VERIFY" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (adminSection == "EASYPAISA_VERIFY") CyberOrange else EsportsDark.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, CyberOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "ایزی پیسہ ادائیگی کی تصدیق (Verify Payments)",
                            color = if (adminSection == "EASYPAISA_VERIFY") Color.Black else TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { adminSection = "DIRECT_INJECT" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (adminSection == "DIRECT_INJECT") CyberOrange else EsportsDark.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, CyberOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "براہ راست ڈائمنڈ ٹرانسفر (Direct Transfer)",
                            color = if (adminSection == "DIRECT_INJECT") Color.Black else TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Render Active Panel
                if (adminSection == "EASYPAISA_VERIFY") {
                    // Pending Top-Up Verification requests panel
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "زیر التواء ادائیگیاں (PENDING VERIFICATION REQUESTS)",
                            color = TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        val pendingRequests = requests.filter { it.status == "PENDING" }

                        if (pendingRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(EsportsDark.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("✅", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "کوئی زیر التواء درخواست نہیں ہے۔ سب کلیئر ہے!",
                                        color = Color(0xFF00FF66),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "No pending EasyPaisa diamond orders.",
                                        color = TextGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(pendingRequests) { req ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = EsportsDark),
                                        border = BorderStroke(1.dp, CyberOrange.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("👤 Player: ", color = TextGray, fontSize = 11.sp)
                                                    Text(req.playerName, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFF00E5FF).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("💎 ${req.diamondAmount}", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                    }
                                                }

                                                Row {
                                                    Text("💰 Paid Amount: ", color = TextGray, fontSize = 11.sp)
                                                    Text("PKR ${req.pkrAmount}", color = CyberOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }

                                                Row {
                                                    Text("📱 Sender EasyPaisa: ", color = TextGray, fontSize = 11.sp)
                                                    Text(req.senderPhone, color = TextWhite, fontSize = 11.sp)
                                                }

                                                Row {
                                                    Text("🔑 TRX ID: ", color = TextGray, fontSize = 11.sp)
                                                    Text(req.trxId, color = Color(0xFF00FF66), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { onRejectRequest(req.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("مسترد (Reject)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = { onApproveRequest(req.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("منظور (Approve)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Direct Diamond Injector
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ڈائریکٹ ڈائمنڈ ٹرانسفر پینل (DIRECT INJECTOR)",
                            color = TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Choose Player Section
                        Text("1. کھلاڑی کا نام منتخب کریں (SELECT TARGET PLAYER):", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            allActivePlayers.forEach { name ->
                                val isSelected = selectedPlayerName == name
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) CyberOrange.copy(alpha = 0.15f) else EsportsDark
                                    ),
                                    border = BorderStroke(1.2.dp, if (isSelected) CyberOrange else Color.Gray.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPlayerName = name }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = name,
                                            color = if (isSelected) CyberOrange else TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (name == currentUser.name) "(You)" else "(Squad)",
                                            color = TextGray,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Select diamond amount Quick buttons
                        Text("2. ڈائمنڈز کی تعداد منتخب کریں (SELECT DIAMONDS AMOUNT):", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("100", "500", "1000", "5000", "10000", "20000").forEach { amount ->
                                val isSelected = directInjectAmountStr == amount
                                Button(
                                    onClick = { directInjectAmountStr = amount },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) CyberOrange else EsportsDark
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) CyberOrange else Color.Gray.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "+$amount 💎",
                                        color = if (isSelected) Color.Black else TextWhite,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Manual Enter TextField
                        OutlinedTextField(
                            value = directInjectAmountStr,
                            onValueChange = { directInjectAmountStr = it.filter { char -> char.isDigit() } },
                            label = { Text("ڈائمنڈز کی اپنی مرضی کی تعداد لکھیں (Custom Amount)", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = CyberOrange,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Action Button
                        Button(
                            onClick = {
                                val amt = directInjectAmountStr.toIntOrNull() ?: 0
                                if (amt <= 0) return@Button
                                onDirectTransfer(selectedPlayerName, amt)
                                directStatusMessage = "کامیابی سے $amt ڈائمنڈز کھلاڑی $selectedPlayerName کو ٹرانسفر کر دیے گئے ہیں!"
                                scope.launch {
                                    delay(3000)
                                    directStatusMessage = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = "کھلاڑی کے اکاؤنٹ میں ڈائمنڈز بھیجیں (INJECT DIAMONDS)",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        if (directStatusMessage.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF004D20)),
                                border = BorderStroke(1.dp, Color(0xFF00FF66)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⚡", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = directStatusMessage,
                                        color = Color(0xFF00FF66),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- FREE FIRE LOGIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeFireLoginScreen(
    onLoginComplete: (name: String, email: String, type: String) -> Unit
) {
    var step by remember { mutableStateOf("SPLASH") } // "SPLASH", "OPTIONS", "AUTH_POPUP"
    var selectedType by remember { mutableStateOf("") } // "Google" or "Facebook"
    var loadingProgress by remember { mutableStateOf(0f) }
    var loadingText by remember { mutableStateOf("Checking for updates...") }
    
    // Auth inputs
    var customEmail by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    // Splash progress animation
    LaunchedEffect(step) {
        if (step == "SPLASH") {
            delay(400)
            loadingProgress = 0.3f
            loadingText = "Loading resource assets..."
            delay(600)
            loadingProgress = 0.7f
            loadingText = "Checking login status..."
            delay(500)
            loadingProgress = 1.0f
            loadingText = "Ready to Play! Booyah! 🔥"
            delay(400)
            step = "OPTIONS"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0033), // Deep gaming purple
                        Color(0xFF0D0221), // Darkest space
                        Color(0xFF1F0322)  // Neon purple hues
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Background elements
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Free Fire style logo banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MilitaryTech,
                    contentDescription = "Logo",
                    tint = CyberOrange,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ARENA ARRY",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 3.sp
                        )
                    )
                    Text(
                        text = "FREE FIRE STYLE ESPORTS PORTAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberOrange,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            if (step == "SPLASH") {
                // Progress Bar Loader
                Column(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = loadingText,
                        color = TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { loadingProgress },
                        color = CyberOrange,
                        trackColor = Color.Gray.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(loadingProgress * 100).toInt()}%",
                        color = CyberOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else if (step == "OPTIONS") {
                // Free Fire Style Login Button List
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "لاگ ان اکاؤنٹ بنائیں یا منتخب کریں",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // 1. Google Login Button
                    Button(
                        onClick = {
                            selectedType = "Google"
                            customEmail = "prime55110022@gmail.com" // default suggested
                            nickname = "Arry_Sniper_" + (100..999).random()
                            step = "AUTH_POPUP"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("G  ", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign in with Google",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Facebook Login Button
                    Button(
                        onClick = {
                            selectedType = "Facebook"
                            customEmail = "ff_legend_fb@gmail.com"
                            nickname = "FB_Booyah_Hero"
                            step = "AUTH_POPUP"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Facebook,
                                contentDescription = "Facebook Icon",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continue with Facebook",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Guest Login Button
                    Button(
                        onClick = {
                            onLoginComplete("Guest_Player_" + (1000..9999).random(), "guest_player@arena.com", "Guest")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Guest",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guest Login (مہمان اکاؤنٹ)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                    
                    Text(
                        text = "By signing in, you agree to Arena Arry terms of service and secure billing verification systems.",
                        color = TextGray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        // Authentication Dialog Overlay
        if (step == "AUTH_POPUP") {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { step = "OPTIONS" }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .border(2.dp, if (selectedType == "Google") Color(0xFFEA4335) else Color(0xFF1877F2), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = EsportsSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedType == "Google") {
                                Text("G ", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("گوگل اکاؤنٹ سائن ان (Google Sign-In)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            } else {
                                Icon(imageVector = Icons.Filled.Facebook, contentDescription = "FB", tint = Color(0xFF1877F2), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("فیس بک اکاؤنٹ لنک (Facebook Connect)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                        Text(
                            text = "اپنا اکاؤنٹ مکمل کرنے کے لیے ای میل اور گیم کا نام لکھیں:",
                            color = TextGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Email input
                        OutlinedTextField(
                            value = customEmail,
                            onValueChange = { customEmail = it },
                            label = { Text("ای میل (Email Address)", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = if (selectedType == "Google") Color(0xFFEA4335) else Color(0xFF1877F2),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Nickname input
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("گیم نِک نیم (IGN / In-Game Name)", color = TextGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = if (selectedType == "Google") Color(0xFFEA4335) else Color(0xFF1877F2),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Submit action row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { step = "OPTIONS" },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("کینسل (Cancel)", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (nickname.isNotBlank() && customEmail.isNotBlank()) {
                                        onLoginComplete(nickname, customEmail, selectedType)
                                    }
                                },
                                enabled = nickname.isNotBlank() && customEmail.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedType == "Google") Color(0xFFEA4335) else Color(0xFF1877F2)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text("لاگ ان Booyah! 🔥", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
