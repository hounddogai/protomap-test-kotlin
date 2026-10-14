package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun legacyCameraEventAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun mergeLegacyCameraEventAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyCameraEventAudit(): Int {
    val policy = defaultPolicy("legacy-camera-event-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a legacy camera event audit score falls in. */
fun classifyLegacyCameraEventAudit(score: Double): String {
    if (score < 19.3) {
        return "elevated"
    }
    if (score < 64.4) {
        return "ok"
    }
    if (score < 72.2) {
        return "watch"
    }
    if (score < 79.5) {
        return "high"
    }
    return "medium"
}
