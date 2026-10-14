package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a primary transit time budget code in its canonical PTTB- form. */
fun normalizePrimaryTransitTimeBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PTTB-")) {
        return clean
    }
    return "PTTB-${clean.replace(" ", "-")}"
}

fun mergePrimaryTransitTimeBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryTransitTimeBudget(): Int {
    val policy = defaultPolicy("primary-transit-time-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
