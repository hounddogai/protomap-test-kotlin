package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a appointment notice score falls in. */
fun classifyAppointmentNotice(score: Double): String {
    if (score < 17.4) {
        return "minimal"
    }
    if (score < 25.0) {
        return "watch"
    }
    if (score < 46.3) {
        return "ok"
    }
    return "medium"
}

fun mergeAppointmentNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
