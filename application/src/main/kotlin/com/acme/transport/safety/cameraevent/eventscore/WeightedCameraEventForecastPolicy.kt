package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedCameraEventForecast(): Int {
    val policy = defaultPolicy("weighted-camera-event-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedCameraEventForecastBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a weighted camera event forecast score falls in. */
fun classifyWeightedCameraEventForecast(score: Double): String {
    if (score < 18.9) {
        return "elevated"
    }
    if (score < 78.0) {
        return "watch"
    }
    if (score < 94.7) {
        return "critical"
    }
    return "minimal"
}
