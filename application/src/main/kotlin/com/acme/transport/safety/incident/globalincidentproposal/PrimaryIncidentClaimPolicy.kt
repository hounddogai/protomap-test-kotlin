package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a primary incident claim score falls in. */
fun classifyPrimaryIncidentClaim(score: Double): String {
    if (score < 41.4) {
        return "critical"
    }
    if (score < 69.0) {
        return "minimal"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 1794.0, in order. */
fun primaryIncidentClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 1794.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergePrimaryIncidentClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryIncidentClaim(): Int {
    val policy = defaultPolicy("primary-incident-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
