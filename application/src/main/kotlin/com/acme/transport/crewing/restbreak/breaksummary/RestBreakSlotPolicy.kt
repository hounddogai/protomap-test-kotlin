package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a rest break slot score falls in. */
fun classifyRestBreakSlot(score: Double): String {
    if (score < 85.5) {
        return "watch"
    }
    if (score < 98.3) {
        return "low"
    }
    return "elevated"
}

/** Puts a rest break slot code in its canonical RBS- form. */
fun normalizeRestBreakSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBS-")) {
        return clean
    }
    return "RBS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakSlot(): Int {
    val policy = defaultPolicy("rest-break-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeRestBreakSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
