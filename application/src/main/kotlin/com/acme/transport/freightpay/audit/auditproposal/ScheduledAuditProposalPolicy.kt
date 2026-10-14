package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a scheduled audit proposal code in its canonical SAP- form. */
fun normalizeScheduledAuditProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAP-")) {
        return clean
    }
    return "SAP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 56737.0, in order. */
fun scheduledAuditProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56737.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeScheduledAuditProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledAuditProposal(): Int {
    val policy = defaultPolicy("scheduled-audit-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
