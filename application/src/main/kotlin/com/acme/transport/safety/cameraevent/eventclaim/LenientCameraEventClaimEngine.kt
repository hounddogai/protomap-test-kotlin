package com.acme.transport.safety.cameraevent.eventclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient camera event claim calculation. */
interface LenientCameraEventClaimFactor {
    fun describe(): String
    fun adjust(item: LenientCameraEventClaim, value: Double): Double
}

data class CapLenientCameraEventClaimAdjuster(var cap: Double) : LenientCameraEventClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientCameraEventClaim, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientCameraEventClaimAdjuster(var step: Double) : LenientCameraEventClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientCameraEventClaim, value: Double): Double = roundTo(value, step)
}

data class MinimumLenientCameraEventClaimAdjuster(var minimum: Double) : LenientCameraEventClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCameraEventClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryLenientCameraEventClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientCameraEventClaimFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientCameraEventClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientCameraEventClaimAdjuster(var percent: Double) : LenientCameraEventClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientCameraEventClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientCameraEventClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientCameraEventClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientCameraEventClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient camera event claim steps in order and keeps a trace of each. */
data class LenientCameraEventClaimEngine(
    var steps: MutableList<LenientCameraEventClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientCameraEventClaim): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientCameraEventClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientCameraEventClaimEngine(flags: Registry): LenientCameraEventClaimEngine {
    return LenientCameraEventClaimEngine(
        steps = mutableListOf<LenientCameraEventClaimFactor>(
            CapLenientCameraEventClaimAdjuster(45563.0),
            RoundingLenientCameraEventClaimAdjuster(5.0),
            MinimumLenientCameraEventClaimAdjuster(3.7),
            CountryLenientCameraEventClaimAdjuster(mutableMapOf<String, Double>("retail" to 0.89)),
            PercentLenientCameraEventClaimAdjuster(-13.8),
            FlaggedLenientCameraEventClaimAdjuster(flags, "lenient-camera-event-claim-batched", -4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 93259.6,
    )
}
