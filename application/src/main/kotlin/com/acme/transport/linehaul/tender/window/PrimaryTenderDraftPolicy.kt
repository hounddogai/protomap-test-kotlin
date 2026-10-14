package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun primaryTenderDraftBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryTenderDraft(): Int {
    val policy = defaultPolicy("primary-tender-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a primary tender draft code in its canonical PTD- form. */
fun normalizePrimaryTenderDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PTD-")) {
        return clean
    }
    return "PTD-${clean.replace(" ", "-")}"
}

/** Names the band a primary tender draft score falls in. */
fun classifyPrimaryTenderDraft(score: Double): String {
    if (score < 14.0) {
        return "minimal"
    }
    if (score < 91.3) {
        return "ok"
    }
    return "medium"
}
