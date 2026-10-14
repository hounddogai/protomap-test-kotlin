package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 79642.0, in order. */
fun strictInspectionProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79642.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeStrictInspectionProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictInspectionProfile(): Int {
    val policy = defaultPolicy("strict-inspection-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a strict inspection profile code in its canonical SIP- form. */
fun normalizeStrictInspectionProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SIP-")) {
        return clean
    }
    return "SIP-${clean.replace(" ", "-")}"
}
