package com.acme.transport.fleetops.defect.defectagreement

/** Puts a batched defect claim code in its canonical BDC- form. */
fun normalizeBatchedDefectClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BDC-")) {
        return clean
    }
    return "BDC-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 98001.0, in order. */
fun batchedDefectClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98001.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedDefectClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a batched defect claim score falls in. */
fun classifyBatchedDefectClaim(score: Double): String {
    if (score < 9.2) {
        return "elevated"
    }
    if (score < 21.0) {
        return "ok"
    }
    return "high"
}
