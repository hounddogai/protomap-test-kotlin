package com.acme.transport.network.zone.group

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a capped zone group score falls in. */
fun classifyCappedZoneGroup(score: Double): String {
    if (score < 11.1) {
        return "critical"
    }
    if (score < 73.9) {
        return "minimal"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedZoneGroup(): Int {
    val policy = defaultPolicy("capped-zone-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
