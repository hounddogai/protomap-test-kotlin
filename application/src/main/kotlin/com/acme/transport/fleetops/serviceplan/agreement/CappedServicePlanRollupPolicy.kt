package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun cappedServicePlanRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeCappedServicePlanRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a capped service plan rollup code in its canonical CSPR- form. */
fun normalizeCappedServicePlanRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CSPR-")) {
        return clean
    }
    return "CSPR-${clean.replace(" ", "-")}"
}

/** Names the band a capped service plan rollup score falls in. */
fun classifyCappedServicePlanRollup(score: Double): String {
    if (score < 2.0) {
        return "watch"
    }
    if (score < 41.6) {
        return "ok"
    }
    if (score < 69.6) {
        return "critical"
    }
    return "medium"
}
