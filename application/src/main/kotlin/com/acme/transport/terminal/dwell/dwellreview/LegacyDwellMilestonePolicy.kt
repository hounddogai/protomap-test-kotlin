package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a legacy dwell milestone score falls in. */
fun classifyLegacyDwellMilestone(score: Double): String {
    if (score < 18.5) {
        return "medium"
    }
    if (score < 30.3) {
        return "watch"
    }
    if (score < 77.6) {
        return "high"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyDwellMilestone(): Int {
    val policy = defaultPolicy("legacy-dwell-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a legacy dwell milestone code in its canonical LDM- form. */
fun normalizeLegacyDwellMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDM-")) {
        return clean
    }
    return "LDM-${clean.replace(" ", "-")}"
}

fun legacyDwellMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
