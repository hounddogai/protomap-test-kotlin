package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a idling ledger code in its canonical IL- form. */
fun normalizeIdlingLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IL-")) {
        return clean
    }
    return "IL-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIdlingLedger(): Int {
    val policy = defaultPolicy("idling-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a idling ledger score falls in. */
fun classifyIdlingLedger(score: Double): String {
    if (score < 19.2) {
        return "critical"
    }
    if (score < 53.4) {
        return "low"
    }
    if (score < 62.7) {
        return "medium"
    }
    return "ok"
}

fun mergeIdlingLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
