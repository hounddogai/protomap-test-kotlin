package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun lockerNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun mergeLockerNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a locker notice code in its canonical LN- form. */
fun normalizeLockerNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LN-")) {
        return clean
    }
    return "LN-${clean.replace(" ", "-")}"
}
