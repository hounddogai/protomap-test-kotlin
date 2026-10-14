package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakMapping(): Int {
    val policy = defaultPolicy("rest-break-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun restBreakMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun mergeRestBreakMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a rest break mapping code in its canonical RBM- form. */
fun normalizeRestBreakMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBM-")) {
        return clean
    }
    return "RBM-${clean.replace(" ", "-")}"
}
