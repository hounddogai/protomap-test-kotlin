package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a manual coaching notice score falls in. */
fun classifyManualCoachingNotice(score: Double): String {
    if (score < 31.6) {
        return "elevated"
    }
    if (score < 51.2) {
        return "critical"
    }
    if (score < 75.7) {
        return "medium"
    }
    return "ok"
}

fun mergeManualCoachingNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
