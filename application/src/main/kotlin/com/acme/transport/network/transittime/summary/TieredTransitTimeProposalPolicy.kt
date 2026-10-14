package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a tiered transit time proposal code in its canonical TTTP- form. */
fun normalizeTieredTransitTimeProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTTP-")) {
        return clean
    }
    return "TTTP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredTransitTimeProposal(): Int {
    val policy = defaultPolicy("tiered-transit-time-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun tieredTransitTimeProposalBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
