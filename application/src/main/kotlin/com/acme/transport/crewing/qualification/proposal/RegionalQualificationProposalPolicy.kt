package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a regional qualification proposal code in its canonical RQP- form. */
fun normalizeRegionalQualificationProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RQP-")) {
        return clean
    }
    return "RQP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalQualificationProposal(): Int {
    val policy = defaultPolicy("regional-qualification-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeRegionalQualificationProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 68111.0, in order. */
fun regionalQualificationProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68111.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
