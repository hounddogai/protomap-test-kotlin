package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 23910.0, in order. */
fun lenientIdlingSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23910.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a lenient idling slot code in its canonical LIS- form. */
fun normalizeLenientIdlingSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LIS-")) {
        return clean
    }
    return "LIS-${clean.replace(" ", "-")}"
}

fun mergeLenientIdlingSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientIdlingSlot(): Int {
    val policy = defaultPolicy("lenient-idling-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
