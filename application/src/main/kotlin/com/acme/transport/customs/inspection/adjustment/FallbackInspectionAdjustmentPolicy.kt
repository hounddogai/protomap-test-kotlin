package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackInspectionAdjustment(): Int {
    val policy = defaultPolicy("fallback-inspection-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a fallback inspection adjustment code in its canonical FIA- form. */
fun normalizeFallbackInspectionAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIA-")) {
        return clean
    }
    return "FIA-${clean.replace(" ", "-")}"
}

/** Names the band a fallback inspection adjustment score falls in. */
fun classifyFallbackInspectionAdjustment(score: Double): String {
    if (score < 10.8) {
        return "minimal"
    }
    if (score < 32.7) {
        return "critical"
    }
    return "high"
}
