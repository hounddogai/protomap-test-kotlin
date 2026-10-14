package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakAssignment(): Int {
    val policy = defaultPolicy("rest-break-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeRestBreakAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rest break assignment score falls in. */
fun classifyRestBreakAssignment(score: Double): String {
    if (score < 51.7) {
        return "low"
    }
    if (score < 57.3) {
        return "watch"
    }
    if (score < 96.9) {
        return "elevated"
    }
    return "minimal"
}

/** Puts a rest break assignment code in its canonical RBA- form. */
fun normalizeRestBreakAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBA-")) {
        return clean
    }
    return "RBA-${clean.replace(" ", "-")}"
}
