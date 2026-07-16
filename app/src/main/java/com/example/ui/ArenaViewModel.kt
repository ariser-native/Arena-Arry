package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.service.GeminiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class ArenaViewModel : ViewModel() {

    // --- State Toggles ---
    private val _currentTab = MutableStateFlow(ArenaTab.LOBBY)
    val currentTab: StateFlow<ArenaTab> = _currentTab.asStateFlow()

    // --- Authentication & Login State ---
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loginType = MutableStateFlow("") // "Google", "Facebook", "Guest"
    val loginType: StateFlow<String> = _loginType.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    // --- Player Profile & Lobby Squad ---
    private val _currentUser = MutableStateFlow(
        Player("Arry_Pro_Sniper", 68, "Heroic", null, isSpeaking = false, isReady = true, isCurrentUser = true, selectedWeaponName = "AWM")
    )
    val currentUser: StateFlow<Player> = _currentUser.asStateFlow()

    private val _squadMembers = MutableStateFlow<List<Player>>(emptyList())
    val squadMembers: StateFlow<List<Player>> = _squadMembers.asStateFlow()

    // --- Mic & Speaker States ---
    private val _isMicEnabled = MutableStateFlow(true)
    val isMicEnabled: StateFlow<Boolean> = _isMicEnabled.asStateFlow()

    private val _isSpeakerEnabled = MutableStateFlow(true)
    val isSpeakerEnabled: StateFlow<Boolean> = _isSpeakerEnabled.asStateFlow()

    // --- Chat System ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // --- Matchmaking State ---
    private val _matchmakingState = MutableStateFlow<MatchmakingStatus>(MatchmakingStatus.Idle)
    val matchmakingState: StateFlow<MatchmakingStatus> = _matchmakingState.asStateFlow()

    private val _matchmakingTimer = MutableStateFlow(0)
    val matchmakingTimer: StateFlow<Int> = _matchmakingTimer.asStateFlow()

    // --- Challenges & Missions ---
    private val _challenges = MutableStateFlow<List<Challenge>>(emptyList())
    val challenges: StateFlow<List<Challenge>> = _challenges.asStateFlow()

    // --- Friend List ---
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    // --- Esports Stats & Points ---
    private val _esportsPoints = MutableStateFlow(2450)
    val esportsPoints: StateFlow<Int> = _esportsPoints.asStateFlow()

    private val _esportsXp = MutableStateFlow(840)
    val esportsXp: StateFlow<Int> = _esportsXp.asStateFlow()

    // --- Diamonds System (Free Fire Style) ---
    private val _diamonds = MutableStateFlow(350)
    val diamonds: StateFlow<Int> = _diamonds.asStateFlow()

    private val _shopItems = MutableStateFlow<List<ShopItem>>(emptyList())
    val shopItems: StateFlow<List<ShopItem>> = _shopItems.asStateFlow()

    private val _unlockedItems = MutableStateFlow<Set<String>>(emptySet())
    val unlockedItems: StateFlow<Set<String>> = _unlockedItems.asStateFlow()

    private val _topUpRequests = MutableStateFlow<List<TopUpRequest>>(emptyList())
    val topUpRequests: StateFlow<List<TopUpRequest>> = _topUpRequests.asStateFlow()

    // --- Weapons Loadout ---
    private val _weapons = MutableStateFlow<List<Weapon>>(emptyList())
    val weapons: StateFlow<List<Weapon>> = _weapons.asStateFlow()

    private val _selectedWeapon = MutableStateFlow<Weapon?>(null)
    val selectedWeapon: StateFlow<Weapon?> = _selectedWeapon.asStateFlow()

    // --- AI Coach Strategy ---
    private val _coachResponse = MutableStateFlow<String>("")
    val coachResponse: StateFlow<String> = _coachResponse.asStateFlow()

    private val _isCoachLoading = MutableStateFlow(false)
    val isCoachLoading: StateFlow<Boolean> = _isCoachLoading.asStateFlow()

    init {
        setupInitialData()
        startVoiceSimulation()
        startRandomTeammateChatSimulation()
    }

    fun selectTab(tab: ArenaTab) {
        _currentTab.value = tab
    }

    private fun setupInitialData() {
        // Weapons list reminiscent of Free Fire
        val weaponList = listOf(
            Weapon("AWM", "Sniper", 95, 15, 90, 95, "Extreme high damage bolt-action sniper rifle. One-shot body headshot potential.", "S"),
            Weapon("M4A1", "Assault Rifle", 60, 56, 54, 70, "Balanced AR with high accuracy and reliable fire rate at mid-to-long ranges.", "A"),
            Weapon("MP40", "SMG", 48, 83, 17, 22, "Extreme close-range SMG with lightning-fast rate of fire. King of rush gameplay.", "S"),
            Weapon("M1887", "Shotgun", 100, 38, 10, 15, "Double-barrel shotgun dealing massive burst damage. Highly lethal in close quarters.", "S"),
            Weapon("SCAR", "Assault Rifle", 53, 61, 41, 60, "Stable assault rifle, very friendly for beginner spray control.", "B")
        )
        _weapons.value = weaponList
        _selectedWeapon.value = weaponList.first()

        // Friends List
        _friends.value = listOf(
            Friend("Rocker_Rush_07", FriendStatus.ONLINE, "Platinum IV", 45),
            Friend("Sniper_Queen", FriendStatus.IN_LOBBY, "Heroic", 58),
            Friend("Gloo_King", FriendStatus.IN_MATCH, "Diamond III", 51),
            Friend("Chrono_Active", FriendStatus.ONLINE, "Grandmaster", 72),
            Friend("Alok_Vibe", FriendStatus.OFFLINE, "Bronze II", 12),
            Friend("Wukong_Bush", FriendStatus.IN_MATCH, "Heroic", 61)
        )

        // Squad Lobby Members
        _squadMembers.value = listOf(
            Player("Rocker_Rush_07", 45, "Platinum IV", null, isSpeaking = false, isReady = true),
            Player("Sniper_Queen", 58, "Heroic", null, isSpeaking = false, isReady = true),
            Player("Chrono_Active", 72, "Grandmaster", null, isSpeaking = false, isReady = false)
        )

        // Challenges / Missions
        _challenges.value = listOf(
            Challenge("ch1", "Clash Squad Rush", "Kill 5 enemies in Clash Squad mode using MP40.", "Clash Squad", 200, 150, 2, 5),
            Challenge("ch2", "AWM Headshot Master", "Get 2 long-range headshots using AWM.", "Battle Royale", 350, 200, 1, 2),
            Challenge("ch3", "Tactical Shielding", "Deploy 12 Gloo Walls to absorb damage.", "All Modes", 150, 100, 12, 12, isCompleted = true),
            Challenge("ch4", "Top Survivor", "Survive in Top 3 positions in Battle Royale.", "Battle Royale", 500, 300, 0, 1)
        )

        // Initial Chat Messages
        _chatMessages.value = listOf(
            ChatMessage("System", "Welcome to Arena Arry Squad Lobby! Mic, Friend requests, and Global challenges are active.", "", isSystem = true),
            ChatMessage("Rocker_Rush_07", "Assalam-o-Alaikum brothers! Match kab start kar rahe ho?", "12:00", isCurrentUser = false),
            ChatMessage("Sniper_Queen", "Walaikum Assalam! Main ready hoon, bas Chrono_Active ko bolo ready kare.", "12:01", isCurrentUser = false)
        )

        _coachResponse.value = "Salam Soldier! Main aapka tactical coach Arry hoon. Aapne AWM select kiya hai, long-range support kijiye aur gloo walls ready rakhein. Aaj ka target double kill hai! Mujhse koi bhi tactical sawal pucho."

        // Initialize Diamond Shop items (Free Fire Theme)
        _shopItems.value = listOf(
            ShopItem("cobra_rage", "Cobra Rage", "Bundle", 1200, "Crimson Cobra outfit with modern special effects, cobra stance, and exclusive animations.", "Legendary", "cobra_pose"),
            ShopItem("sakura_bundle", "Sakura Festival", "Bundle", 1500, "Season 1 elite samurai traditional attire. High-status symbol!", "Legendary", "sakura_pose"),
            ShopItem("green_dino", "Green Dino", "Bundle", 800, "Cute green full-body dinosaur jumpsuit. Extremely rare street style.", "Epic", "dino_hop"),
            ShopItem("hip_hop", "Hip Hop Star", "Bundle", 1000, "Season 2 elite street dancing neon jacket with golden chains.", "Legendary", "hiphop_dance"),
            ShopItem("arctic_blue", "Arctic Blue", "Bundle", 900, "Futuristic cyber-ninja combat suit glowing with icy blue energy.", "Epic", "arctic_glow"),
            ShopItem("throne_emote", "FFWC Throne", "Emote", 500, "Deploy and sit on a glorious golden esports championship throne.", "Legendary", "sit_on_throne"),
            ShopItem("love_emote", "Flowers of Love", "Emote", 400, "Kneel down and present a beautiful bouquet of red roses.", "Legendary", "propose_flower"),
            ShopItem("selfie_emote", "Selfie Flash", "Emote", 300, "Pull out a holograph smartphone and take a stylized victory photo.", "Epic", "take_selfie"),
            ShopItem("lol_emote", "LOL Laugh", "Emote", 250, "Burst out laughing to teasingly celebrate knocking down an opponent.", "Rare", "laugh_lol"),
            ShopItem("booyah_balloon", "Booyah Balloon", "Emote", 350, "Inflate a large neon Booyah balloon and bounce high on it.", "Epic", "inflate_balloon")
        )

        // Mock Top-up requests submitted by player accounts to Owner (Ansa Tasnim) EasyPaisa
        _topUpRequests.value = listOf(
            TopUpRequest("req_101", "Rocker_Rush_07", 300, 255, "TRX-884729103", "03017726481", "PENDING"),
            TopUpRequest("req_102", "Sniper_Queen", 1000, 875, "TRX-992104827", "03335562911", "PENDING"),
            TopUpRequest("req_103", "Gloo_King", 100, 85, "TRX-123984720", "03125556677", "APPROVED")
        )
    }

    // --- Voice simulation loop ---
    private fun startVoiceSimulation() {
        viewModelScope.launch {
            val random = Random()
            while (true) {
                delay(2000 + random.nextInt(3000).toLong())
                if (_isSpeakerEnabled.value) {
                    val members = _squadMembers.value
                    if (members.isNotEmpty()) {
                        val speakerIndex = random.nextInt(members.size)
                        val updated = members.mapIndexed { index, player ->
                            if (index == speakerIndex) player.copy(isSpeaking = !player.isSpeaking)
                            else player.copy(isSpeaking = false)
                        }
                        _squadMembers.value = updated
                    }
                } else {
                    _squadMembers.value = _squadMembers.value.map { it.copy(isSpeaking = false) }
                }
            }
        }
    }

    // --- Random Teammate chat messages simulation ---
    private fun startRandomTeammateChatSimulation() {
        viewModelScope.launch {
            val random = Random()
            val responses = listOf(
                "Ajao bro, rush marenge safe zone ke andar!",
                "Koi aage se cover fire do, mere paas short-range gun nahi h",
                "Arry AI se pucho na konsi strategy best rahegi is loadout pe!",
                "Chrono active ready kar do jaldi se game lagaein",
                "Opponent team me grandmaster players hain be careful!",
                "Mere paas extra gloo wall h, chahiye to bolo",
                "Nice sniping Arry bro! Obe headshot tha!",
                "M1887 shotty OP in close combat!"
            )
            val names = listOf("Rocker_Rush_07", "Sniper_Queen", "Chrono_Active")

            while (true) {
                delay(12000 + random.nextInt(10000).toLong())
                // Only post if not matchmaking or starting
                if (_matchmakingState.value is MatchmakingStatus.Idle) {
                    val sender = names[random.nextInt(names.size)]
                    val text = responses[random.nextInt(responses.size)]
                    addMessage(ChatMessage(sender, text, getCurrentTimeString()))
                }
            }
        }
    }

    private fun getCurrentTimeString(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    // --- Custom Weapon Selection ---
    fun selectWeapon(weapon: Weapon) {
        _selectedWeapon.value = weapon
        _currentUser.value = _currentUser.value.copy(selectedWeaponName = weapon.name)
        
        // Automated strategy feedback from Coach when loadout changes
        viewModelScope.launch {
            _coachResponse.value = "Arry is analyzing the ${weapon.name} loadout..."
            _isCoachLoading.value = true
            delay(1000)
            val weaponStrategy = when (weapon.name) {
                "AWM" -> "AWM loadout selected! 🔥 Ye extreme range weapon hai. Hamesha high ground hold karein, secondary gun MP40 rakhein close defense k liye."
                "M4A1" -> "M4A1 reliable choice hai. Silencer lagakar spray karein mid-range me, recoil control bohot easy hai."
                "MP40" -> "MP40 rushers ki pehli pasand! 🔥 Aggressive khelna hai to perfect hai, zigzag movement ke sath spray karein."
                "M1887" -> "M1887 double-barrel, 2 shot me enemy finished! Hamesha jump-shot aur gloo wall cover combine karein."
                "SCAR" -> "SCAR is super stable. Scope-in spray technique seekhein iske liye, stable long distance damage milega."
                else -> "Great gun selection! Optimize your tactical loadout and keep communicating with the squad."
            }
            _coachResponse.value = weaponStrategy
            _isCoachLoading.value = false
        }
    }

    // --- Chat Actions ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(_currentUser.value.name, text, getCurrentTimeString(), isCurrentUser = true)
        addMessage(userMsg)

        // Teammates mock reply or AI coach automated reply trigger
        viewModelScope.launch {
            delay(1000)
            if (text.lowercase().contains("arry") || text.lowercase().contains("coach") || text.lowercase().contains("help")) {
                askAiCoach(text)
            } else {
                val teamReplies = listOf(
                    "Agreed! Let's follow this.",
                    "Sahi bol rhe ho, strategy badlo.",
                    "Haa bro, zone shrink hone wala h jaldi",
                    "Ajao party join karo"
                )
                val reply = teamReplies[Random().nextInt(teamReplies.size)]
                val name = listOf("Sniper_Queen", "Rocker_Rush_07", "Chrono_Active")[Random().nextInt(3)]
                addMessage(ChatMessage(name, reply, getCurrentTimeString()))
            }
        }
    }

    private fun addMessage(msg: ChatMessage) {
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(msg)
        // Keep last 40 messages to prevent excessive memory
        if (currentList.size > 40) {
            currentList.removeAt(0)
        }
        _chatMessages.value = currentList
    }

    // --- Matchmaking / Finding Match Logic ---
    fun startMatchmaking() {
        if (_matchmakingState.value != MatchmakingStatus.Idle) return

        _matchmakingState.value = MatchmakingStatus.Finding
        _matchmakingTimer.value = 0

        viewModelScope.launch {
            // Simulator timer
            while (_matchmakingState.value == MatchmakingStatus.Finding) {
                delay(1000)
                _matchmakingTimer.value += 1

                // Simulate finding game after some random seconds (usually between 8 and 15 seconds)
                if (_matchmakingTimer.value >= 10 && Random().nextInt(10) > 6) {
                    _matchmakingState.value = MatchmakingStatus.Found
                    delay(2500) // Connecting time
                    _matchmakingState.value = MatchmakingStatus.Starting(5)

                    // Countdown to match launch
                    for (i in 5 downTo 1) {
                        _matchmakingState.value = MatchmakingStatus.Starting(i)
                        delay(1000)
                    }

                    // Simulated match entry, add system notification in chat
                    _matchmakingState.value = MatchmakingStatus.InGame
                    addMessage(ChatMessage("System", "Match launched! 🪂 Airborne drop in progress. Safe zone active.", "", isSystem = true))
                    
                    delay(15000) // Live game simulation for 15s
                    
                    // Match Over simulation
                    val xpEarned = 150 + Random().nextInt(150)
                    val pointsEarned = 25 + Random().nextInt(30)
                    _esportsPoints.value += pointsEarned
                    _esportsXp.value += xpEarned
                    
                    addMessage(ChatMessage("System", "Match Finished! 🏆 VICTORY (BOOYAH!) +${pointsEarned} Points, +${xpEarned} XP", "", isSystem = true))
                    _matchmakingState.value = MatchmakingStatus.Idle
                }
            }
        }
    }

    fun cancelMatchmaking() {
        _matchmakingState.value = MatchmakingStatus.Idle
        _matchmakingTimer.value = 0
    }

    // --- Friend Invites / Mock adding ---
    fun toggleFriendStatusInLobby(friend: Friend) {
        val squad = _squadMembers.value.toMutableList()
        val alreadyInSquad = squad.any { it.name == friend.name }

        if (alreadyInSquad) {
            squad.removeAll { it.name == friend.name }
            _squadMembers.value = squad
            addMessage(ChatMessage("System", "${friend.name} left the squad lobby.", "", isSystem = true))
        } else {
            if (squad.size >= 3) {
                addMessage(ChatMessage("System", "Lobby is full! Max 4 players in a squad.", "", isSystem = true))
                return
            }
            squad.add(Player(friend.name, friend.level, friend.rank, null, isSpeaking = false, isReady = true))
            _squadMembers.value = squad
            addMessage(ChatMessage("System", "${friend.name} joined the squad lobby.", "", isSystem = true))
        }

        // Update friends status to reflect In Lobby
        val updatedFriends = _friends.value.map {
            if (it.name == friend.name) {
                it.copy(status = if (alreadyInSquad) FriendStatus.ONLINE else FriendStatus.IN_LOBBY)
            } else it
        }
        _friends.value = updatedFriends
    }

    fun addMockFriend(name: String) {
        if (name.isBlank()) return
        val current = _friends.value.toMutableList()
        if (current.any { it.name == name }) return
        current.add(0, Friend(name, FriendStatus.ONLINE, "Platinum I", 30))
        _friends.value = current
        addMessage(ChatMessage("System", "$name is now your friend! Invite them to the lobby.", "", isSystem = true))
    }

    // --- Challenge Claims ---
    fun buyEsportsPoints(amount: Int) {
        _esportsPoints.value += amount
        addMessage(ChatMessage("System", "Top-up successful! Added $amount Esports Points (EP) to your account.", "", isSystem = true))
    }

    // --- Diamond Shop & EasyPaisa Top-up Logic ---
    fun submitTopUpRequest(pkr: Int, diamonds: Int, trxId: String, phone: String) {
        val reqId = "req_" + System.currentTimeMillis().toString().takeLast(6)
        val newRequest = TopUpRequest(
            id = reqId,
            playerName = _currentUser.value.name,
            pkrAmount = pkr,
            diamondAmount = diamonds,
            trxId = trxId,
            senderPhone = phone,
            status = "PENDING"
        )
        val list = _topUpRequests.value.toMutableList()
        list.add(0, newRequest)
        _topUpRequests.value = list
        
        addMessage(ChatMessage("System", "آپ کی EasyPaisa ادائیگی کی درخواست موصول ہو گئی ہے۔ رقم کی تصدیق کے بعد ڈائمنڈز ٹرانسفر کر دیے جائیں گے۔ (Request ID: $reqId)", "", isSystem = true))
    }

    fun approveTopUpRequest(reqId: String) {
        val requests = _topUpRequests.value.map { req ->
            if (req.id == reqId && req.status == "PENDING") {
                if (req.playerName == _currentUser.value.name) {
                    _diamonds.value += req.diamondAmount
                }
                
                addMessage(
                    ChatMessage(
                        sender = "System",
                        message = "Top-up approved! ${req.diamondAmount} Diamonds credited to ${req.playerName}.",
                        timestamp = getCurrentTimeString(),
                        isSystem = true
                    )
                )
                req.copy(status = "APPROVED")
            } else req
        }
        _topUpRequests.value = requests
    }

    fun rejectTopUpRequest(reqId: String) {
        val requests = _topUpRequests.value.map { req ->
            if (req.id == reqId && req.status == "PENDING") {
                addMessage(
                    ChatMessage(
                        sender = "System",
                        message = "Top-up request rejected for ${req.playerName} (TRX: ${req.trxId}). Contact support.",
                        timestamp = getCurrentTimeString(),
                        isSystem = true
                    )
                )
                req.copy(status = "REJECTED")
            } else req
        }
        _topUpRequests.value = requests
    }

    fun directTransferDiamonds(playerName: String, amount: Int) {
        if (playerName == _currentUser.value.name) {
            _diamonds.value += amount
        }
        addMessage(
            ChatMessage(
                sender = "Owner (Ansa Tasnim)",
                message = "Transfer complete! Instantly sent $amount diamonds to $playerName.",
                timestamp = getCurrentTimeString(),
                isSystem = true
            )
        )
    }

    fun purchaseShopItem(itemId: String): Boolean {
        val item = _shopItems.value.find { it.id == itemId } ?: return false
        val currentDiamonds = _diamonds.value
        
        if (_unlockedItems.value.contains(itemId)) {
            addMessage(ChatMessage("System", "You already own the ${item.name}!", "", isSystem = true))
            return false
        }
        
        if (currentDiamonds >= item.price) {
            _diamonds.value -= item.price
            val currentUnlocked = _unlockedItems.value.toMutableSet()
            currentUnlocked.add(itemId)
            _unlockedItems.value = currentUnlocked
            
            addMessage(
                ChatMessage(
                    sender = "System",
                    message = "Congratulations! You purchased the legendary ${item.name} ${item.category} for ${item.price} diamonds! Booyah! 🎉",
                    timestamp = getCurrentTimeString(),
                    isSystem = true
                )
            )
            return true
        } else {
            addMessage(ChatMessage("System", "نا کافی ڈائمنڈز! براہ کرم ڈائمنڈز ٹاپ اپ کریں۔ (Insufficient Diamonds!)", "", isSystem = true))
            return false
        }
    }

    fun claimChallenge(challengeId: String) {
        val updated = _challenges.value.map {
            if (it.id == challengeId) {
                _esportsPoints.value += it.rewardPoints
                _esportsXp.value += it.xpReward
                it.copy(progress = it.maxProgress, isCompleted = true)
            } else it
        }
        _challenges.value = updated
    }

    // --- Mic and Speaker toggles ---
    fun toggleMic() {
        _isMicEnabled.value = !_isMicEnabled.value
        _currentUser.value = _currentUser.value.copy(isSpeaking = false)
        val status = if (_isMicEnabled.value) "enabled" else "disabled"
        addMessage(ChatMessage("System", "Your mic has been $status.", "", isSystem = true))
    }

    fun toggleSpeaker() {
        _isSpeakerEnabled.value = !_isSpeakerEnabled.value
        val status = if (_isSpeakerEnabled.value) "enabled" else "disabled"
        addMessage(ChatMessage("System", "Squad speaker sound $status.", "", isSystem = true))
    }

    // --- AI Coach Custom Prompts (Gemini Integration) ---
    fun askAiCoach(prompt: String) {
        if (prompt.isBlank()) return
        _isCoachLoading.value = true
        _coachResponse.value = "Analyzing strategy..."

        viewModelScope.launch {
            val response = GeminiService.getTacticalAdvice(prompt)
            _coachResponse.value = response
            _isCoachLoading.value = false
            
            // Add to chat so team can see the coach's wisdom
            addMessage(ChatMessage("Coach Arry", response, getCurrentTimeString(), isCoach = true))
        }
    }

    // --- SharedPreferences & Authentication Persistence ---
    fun performLogin(name: String, email: String, loginType: String, context: android.content.Context) {
        val userPref = context.getSharedPreferences("arena_user_prefs", android.content.Context.MODE_PRIVATE)
        userPref.edit().apply {
            putBoolean("is_logged_in", true)
            putString("player_name", name)
            putString("player_email", email)
            putString("login_type", loginType)
            putInt("player_level", 55)
            putString("player_rank", "Heroic")
            apply()
        }

        _currentUser.value = Player(
            name = name,
            level = 55,
            rank = "Heroic",
            avatarRes = null,
            isSpeaking = false,
            isReady = true,
            isCurrentUser = true,
            selectedWeaponName = _selectedWeapon.value?.name ?: "M4A1"
        )
        _isLoggedIn.value = true
        _loginType.value = loginType
        _userEmail.value = email

        addMessage(
            ChatMessage(
                sender = "System",
                message = "Welcome $name! Account created successfully via $loginType. Let's Booyah! 🔥🎮",
                timestamp = getCurrentTimeString(),
                isSystem = true
            )
        )
    }

    fun checkStoredLogin(context: android.content.Context) {
        val userPref = context.getSharedPreferences("arena_user_prefs", android.content.Context.MODE_PRIVATE)
        val storedLoggedIn = userPref.getBoolean("is_logged_in", false)
        if (storedLoggedIn) {
            val name = userPref.getString("player_name", "Arry_Pro_Sniper") ?: "Arry_Pro_Sniper"
            val email = userPref.getString("player_email", "") ?: ""
            val type = userPref.getString("login_type", "Guest") ?: "Guest"
            val level = userPref.getInt("player_level", 55)
            val rank = userPref.getString("player_rank", "Heroic") ?: "Heroic"

            _currentUser.value = Player(
                name = name,
                level = level,
                rank = rank,
                avatarRes = null,
                isSpeaking = false,
                isReady = true,
                isCurrentUser = true,
                selectedWeaponName = _selectedWeapon.value?.name ?: "M4A1"
            )
            _isLoggedIn.value = true
            _loginType.value = type
            _userEmail.value = email
        }
    }

    fun performLogout(context: android.content.Context) {
        val userPref = context.getSharedPreferences("arena_user_prefs", android.content.Context.MODE_PRIVATE)
        userPref.edit().clear().apply()
        
        _isLoggedIn.value = false
        _loginType.value = ""
        _userEmail.value = ""
        _currentUser.value = Player(
            name = "Guest_Player",
            level = 1,
            rank = "Bronze I",
            avatarRes = null,
            isSpeaking = false,
            isReady = false,
            isCurrentUser = true,
            selectedWeaponName = "M4A1"
        )
    }
}

enum class ArenaTab {
    LOBBY,
    CHALLENGES,
    WEAPONS,
    COACH
}

sealed class MatchmakingStatus {
    object Idle : MatchmakingStatus()
    object Finding : MatchmakingStatus()
    object Found : MatchmakingStatus()
    data class Starting(val countdown: Int) : MatchmakingStatus()
    object InGame : MatchmakingStatus()
}
