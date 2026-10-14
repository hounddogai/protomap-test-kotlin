package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 86054.0, in order. */
fun adaptiveTransitTimePreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86054.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a adaptive transit time preference code in its canonical ATTP- form. */
fun normalizeAdaptiveTransitTimePreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ATTP-")) {
        return clean
    }
    return "ATTP-${clean.replace(" ", "-")}"
}

fun mergeAdaptiveTransitTimePreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveTransitTimePreference(): Int {
    val policy = defaultPolicy("adaptive-transit-time-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
