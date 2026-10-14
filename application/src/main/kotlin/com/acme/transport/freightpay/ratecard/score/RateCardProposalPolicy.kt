package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.kit.numbers.roundTo

/** Names the band a rate card proposal score falls in. */
fun classifyRateCardProposal(score: Double): String {
    if (score < 48.5) {
        return "ok"
    }
    if (score < 85.4) {
        return "medium"
    }
    return "watch"
}

fun rateCardProposalBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
