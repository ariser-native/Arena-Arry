package com.example.model

data class Player(
    val name: String,
    val level: Int,
    val rank: String, // e.g. "Bronze I", "Platinum IV", "Heroic", "Grandmaster"
    val avatarRes: Int?, // Can be our generated image or a standard emoji/placeholder
    val isSpeaking: Boolean = false,
    val isReady: Boolean = false,
    val isCurrentUser: Boolean = false,
    val selectedWeaponName: String = "M4A1"
)

data class ChatMessage(
    val sender: String,
    val message: String,
    val timestamp: String,
    val isSystem: Boolean = false,
    val isCoach: Boolean = false,
    val isCurrentUser: Boolean = false
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val gameMode: String, // e.g. "Battle Royale", "Clash Squad"
    val rewardPoints: Int,
    val xpReward: Int,
    val progress: Int,
    val maxProgress: Int,
    val isCompleted: Boolean = false
)

data class Friend(
    val name: String,
    val status: FriendStatus,
    val rank: String,
    val level: Int
)

enum class FriendStatus {
    ONLINE,
    IN_LOBBY,
    IN_MATCH,
    OFFLINE
}

data class Weapon(
    val name: String,
    val type: String, // e.g. "Assault Rifle", "Sniper", "SMG", "Shotgun"
    val damage: Int, // 0 to 100
    val fireRate: Int, // 0 to 100
    val accuracy: Int, // 0 to 100
    val range: Int, // 0 to 100
    val description: String,
    val tier: String // "S", "A", "B"
)

data class ShopItem(
    val id: String,
    val name: String,
    val category: String, // "Emote" or "Bundle"
    val price: Int, // in Diamonds
    val description: String,
    val rarity: String, // "Legendary", "Epic", "Rare"
    val emoteAction: String = "" // For animations
)

data class TopUpRequest(
    val id: String,
    val playerName: String,
    val pkrAmount: Int,
    val diamondAmount: Int,
    val trxId: String,
    val senderPhone: String,
    val status: String = "PENDING" // "PENDING", "APPROVED", "REJECTED"
)
