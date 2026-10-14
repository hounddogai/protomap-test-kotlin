package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyViolationVariance(): Int {
    val policy = defaultPolicy("legacy-violation-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a legacy violation variance code in its canonical LVV- form. */
fun normalizeLegacyViolationVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LVV-")) {
        return clean
    }
    return "LVV-${clean.replace(" ", "-")}"
}

fun mergeLegacyViolationVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
