package com.acme.transport.customs.tariff.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualTariffVariance(): Int {
    val policy = defaultPolicy("manual-tariff-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a manual tariff variance code in its canonical MTV- form. */
fun normalizeManualTariffVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTV-")) {
        return clean
    }
    return "MTV-${clean.replace(" ", "-")}"
}

fun mergeManualTariffVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a manual tariff variance score falls in. */
fun classifyManualTariffVariance(score: Double): String {
    if (score < 14.0) {
        return "ok"
    }
    if (score < 92.6) {
        return "low"
    }
    return "medium"
}
