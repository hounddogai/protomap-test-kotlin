package com.acme.transport.linehaul.laneplan.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a lane plan slot score falls in. */
fun classifyLanePlanSlot(score: Double): String {
    if (score < 16.9) {
        return "watch"
    }
    if (score < 98.5) {
        return "minimal"
    }
    return "elevated"
}

/** Puts a lane plan slot code in its canonical LPS- form. */
fun normalizeLanePlanSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPS-")) {
        return clean
    }
    return "LPS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLanePlanSlot(): Int {
    val policy = defaultPolicy("lane-plan-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeLanePlanSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
