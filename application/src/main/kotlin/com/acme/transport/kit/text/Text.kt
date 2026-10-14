package com.acme.transport.kit.text

// Small string helpers shared by every area.

/** Trims a value and lowers its case, so lookups ignore how people typed it. */
fun normalize(s: String): String {
    val trimmed = s.trim()
    if (trimmed.length == 0) {
        return ""
    }
    return trimmed.lowercase()
}

/** Turns a display name into a lowercase, dash-separated key. */
fun slugify(s: String): String {
    val parts = s.trim().lowercase().split(" ").toMutableList()
    val kept = mutableListOf<String>()
    for (part in parts) {
        if (part.length > 0) {
            kept.add(part)
        }
    }
    return kept.joinToString("-")
}

/** Shortens a value to at most limit characters, marking the cut with an ellipsis. */
fun truncate(s: String, limit: Int): String {
    if (s.length <= limit) {
        return s
    }
    if (limit <= 3) {
        return s.take(limit)
    }
    return s.take(limit - 3) + "..."
}

/** Masks all but the last keep characters, as receipts and logs show references. */
fun maskTail(s: String, keep: Int): String {
    if (s.length <= keep) {
        return s
    }
    val hidden = s.length - keep
    return "*".repeat(hidden) + s.takeLast(keep)
}

fun wordCount(s: String): Int {
    var count = 0
    for (word in s.trim().split(" ").toMutableList()) {
        if (word.length > 0) {
            count += 1
        }
    }
    return count
}

/** Reports whether any of the words occurs in s, ignoring case. */
fun containsAny(s: String, words: MutableList<String>): Boolean {
    val lowered = s.lowercase()
    for (word in words) {
        if (lowered.contains(word.lowercase())) {
            return true
        }
    }
    return false
}

fun joinNonEmpty(parts: MutableList<String>, sep: String): String {
    val kept = mutableListOf<String>()
    for (part in parts) {
        val clean = part.trim()
        if (clean != "") {
            kept.add(clean)
        }
    }
    return kept.joinToString(sep)
}

fun firstNonEmpty(parts: MutableList<String>): String {
    for (part in parts) {
        if (part.trim() != "") {
            return part
        }
    }
    return ""
}

/** Pads or cuts a value to exactly width characters, for fixed-width output. */
fun fit(s: String, width: Int): String {
    if (s.length > width) {
        return s.take(width)
    }
    return s.padEnd(width, ' ')
}
