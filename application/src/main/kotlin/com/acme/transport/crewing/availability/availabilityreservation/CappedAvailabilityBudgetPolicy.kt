package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a capped availability budget code in its canonical CAB- form. */
fun normalizeCappedAvailabilityBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CAB-")) {
        return clean
    }
    return "CAB-${clean.replace(" ", "-")}"
}

fun mergeCappedAvailabilityBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedAvailabilityBudget(): Int {
    val policy = defaultPolicy("capped-availability-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
