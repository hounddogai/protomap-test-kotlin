package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedRosterManifest(): Int {
    val policy = defaultPolicy("capped-roster-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a capped roster manifest score falls in. */
fun classifyCappedRosterManifest(score: Double): String {
    if (score < 18.8) {
        return "ok"
    }
    if (score < 31.4) {
        return "critical"
    }
    return "watch"
}

fun cappedRosterManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
