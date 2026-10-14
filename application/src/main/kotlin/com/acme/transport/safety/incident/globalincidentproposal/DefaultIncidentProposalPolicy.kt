package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultIncidentProposal(): Int {
    val policy = defaultPolicy("default-incident-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 30570.0, in order. */
fun defaultIncidentProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30570.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a default incident proposal code in its canonical DIP- form. */
fun normalizeDefaultIncidentProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DIP-")) {
        return clean
    }
    return "DIP-${clean.replace(" ", "-")}"
}

fun defaultIncidentProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
