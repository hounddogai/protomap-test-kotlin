package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a fallback route offset schedule code in its canonical FROS- form. */
fun normalizeFallbackRouteOffsetScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FROS-")) {
        return clean
    }
    return "FROS-${clean.replace(" ", "-")}"
}

fun mergeFallbackRouteOffsetScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackRouteOffsetSchedule(): Int {
    val policy = defaultPolicy("fallback-route-offset-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
