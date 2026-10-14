package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun primaryTrailerNoticeBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergePrimaryTrailerNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun primaryTrailerNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a primary trailer notice score falls in. */
fun classifyPrimaryTrailerNotice(score: Double): String {
    if (score < 17.9) {
        return "ok"
    }
    if (score < 61.1) {
        return "minimal"
    }
    return "critical"
}
