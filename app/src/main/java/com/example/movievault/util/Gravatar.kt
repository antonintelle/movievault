package com.example.movievault.util

import java.security.MessageDigest

fun gravatarUrl(email: String, size: Int = 200): String {
    val clean = email.trim().lowercase()
    val md5 = MessageDigest.getInstance("MD5")
        .digest(clean.toByteArray())
        .joinToString("") { "%02x".format(it) }

    return "https://www.gravatar.com/avatar/$md5?s=$size&d=identicon"
}
