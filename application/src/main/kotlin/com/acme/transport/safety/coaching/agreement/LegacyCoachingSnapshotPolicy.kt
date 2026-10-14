package com.acme.transport.safety.coaching.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a legacy coaching snapshot code in its canonical LCS- form. */
fun normalizeLegacyCoachingSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCS-")) {
        return clean
    }
    return "LCS-${clean.replace(" ", "-")}"
}

fun legacyCoachingSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyCoachingSnapshot(): Int {
    val policy = defaultPolicy("legacy-coaching-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 88650.0, in order. */
fun legacyCoachingSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88650.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
