package com.acme.transport.emissions.routeoffset.offsetmapping

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 96283.0, in order. */
fun fallbackRouteOffsetMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96283.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackRouteOffsetMapping(): Int {
    val policy = defaultPolicy("fallback-route-offset-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fallback route offset mapping score falls in. */
fun classifyFallbackRouteOffsetMapping(score: Double): String {
    if (score < 3.1) {
        return "watch"
    }
    if (score < 9.2) {
        return "low"
    }
    if (score < 31.8) {
        return "minimal"
    }
    return "critical"
}

/** Puts a fallback route offset mapping code in its canonical FROM- form. */
fun normalizeFallbackRouteOffsetMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FROM-")) {
        return clean
    }
    return "FROM-${clean.replace(" ", "-")}"
}
