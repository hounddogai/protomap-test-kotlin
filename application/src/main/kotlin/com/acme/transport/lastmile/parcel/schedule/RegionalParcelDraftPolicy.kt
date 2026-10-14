package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalParcelDraft(): Int {
    val policy = defaultPolicy("regional-parcel-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a regional parcel draft score falls in. */
fun classifyRegionalParcelDraft(score: Double): String {
    if (score < 91.5) {
        return "low"
    }
    if (score < 92.6) {
        return "elevated"
    }
    return "high"
}
