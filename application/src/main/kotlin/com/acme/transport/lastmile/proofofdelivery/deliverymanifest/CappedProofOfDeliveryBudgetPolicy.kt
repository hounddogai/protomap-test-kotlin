package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedProofOfDeliveryBudget(): Int {
    val policy = defaultPolicy("capped-proof-of-delivery-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped proof of delivery budget code in its canonical CPODB- form. */
fun normalizeCappedProofOfDeliveryBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CPODB-")) {
        return clean
    }
    return "CPODB-${clean.replace(" ", "-")}"
}

fun mergeCappedProofOfDeliveryBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capped proof of delivery budget score falls in. */
fun classifyCappedProofOfDeliveryBudget(score: Double): String {
    if (score < 16.8) {
        return "low"
    }
    if (score < 27.9) {
        return "minimal"
    }
    if (score < 32.8) {
        return "medium"
    }
    if (score < 35.5) {
        return "high"
    }
    return "ok"
}
