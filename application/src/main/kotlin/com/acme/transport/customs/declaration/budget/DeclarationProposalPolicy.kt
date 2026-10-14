package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a declaration proposal score falls in. */
fun classifyDeclarationProposal(score: Double): String {
    if (score < 22.6) {
        return "high"
    }
    if (score < 77.6) {
        return "minimal"
    }
    if (score < 85.9) {
        return "low"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 2518.0, in order. */
fun declarationProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2518.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeDeclarationProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationProposal(): Int {
    val policy = defaultPolicy("declaration-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
