package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackLanePlanBundle(): Int {
    val policy = defaultPolicy("fallback-lane-plan-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun fallbackLanePlanBundleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a fallback lane plan bundle code in its canonical FLPB- form. */
fun normalizeFallbackLanePlanBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FLPB-")) {
        return clean
    }
    return "FLPB-${clean.replace(" ", "-")}"
}
