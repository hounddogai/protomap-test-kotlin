package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalClearanceNotice(): Int {
    val policy = defaultPolicy("seasonal-clearance-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a seasonal clearance notice code in its canonical SCN- form. */
fun normalizeSeasonalClearanceNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCN-")) {
        return clean
    }
    return "SCN-${clean.replace(" ", "-")}"
}

fun mergeSeasonalClearanceNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
