package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy camera event claim calculation. */
interface LegacyCameraEventClaimStep {
    fun describe(): String
    fun adjust(item: LegacyCameraEventClaim, value: Double): Double
}

data class CapLegacyCameraEventClaimAdjuster(var cap: Double) : LegacyCameraEventClaimStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyCameraEventClaim, value: Double): Double = minOf(value, cap)
}

data class FlaggedLegacyCameraEventClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyCameraEventClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyCameraEventClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLegacyCameraEventClaimAdjuster(var minimum: Double) : LegacyCameraEventClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyCameraEventClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyCameraEventClaimAdjuster(var percent: Double) : LegacyCameraEventClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyCameraEventClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy camera event claim steps in order and keeps a trace of each. */
data class LegacyCameraEventClaimEngine(
    var steps: MutableList<LegacyCameraEventClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyCameraEventClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyCameraEventClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyCameraEventClaimEngine(flags: Registry): LegacyCameraEventClaimEngine {
    return LegacyCameraEventClaimEngine(
        steps = mutableListOf<LegacyCameraEventClaimStep>(
            CapLegacyCameraEventClaimAdjuster(18230.0),
            FlaggedLegacyCameraEventClaimAdjuster(flags, "legacy-camera-event-claim-regional", 1.6),
            MinimumLegacyCameraEventClaimAdjuster(33.3),
            PercentLegacyCameraEventClaimAdjuster(-4.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 3541.8,
    )
}
