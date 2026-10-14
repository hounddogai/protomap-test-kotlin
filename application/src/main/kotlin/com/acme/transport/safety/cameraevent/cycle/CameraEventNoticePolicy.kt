package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCameraEventNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a camera event notice score falls in. */
fun classifyCameraEventNotice(score: Double): String {
    if (score < 10.1) {
        return "low"
    }
    if (score < 11.1) {
        return "watch"
    }
    if (score < 91.3) {
        return "medium"
    }
    return "critical"
}
