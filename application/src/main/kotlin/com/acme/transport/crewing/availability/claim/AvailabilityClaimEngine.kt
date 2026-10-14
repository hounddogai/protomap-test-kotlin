package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the availability claim calculation. */
interface AvailabilityClaimStep {
    fun describe(): String
    fun adjust(item: AvailabilityClaim, value: Double): Double
}

data class TieredAvailabilityClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilityClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilityClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAvailabilityClaimAdjuster(var minimum: Double) : AvailabilityClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAvailabilityClaimAdjuster(var step: Double) : AvailabilityClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityClaim, value: Double): Double = roundTo(value, step)
}

data class FlaggedAvailabilityClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AvailabilityClaimBatchSizeOffsetAdjuster(var weight: Double) : AvailabilityClaimStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: AvailabilityClaim, value: Double): Double = value + item.batchSize.toDouble() * weight
}

/** Runs the availability claim steps in order and keeps a trace of each. */
data class AvailabilityClaimEngine(
    var steps: MutableList<AvailabilityClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AvailabilityClaim): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AvailabilityClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAvailabilityClaimEngine(flags: Registry): AvailabilityClaimEngine {
    return AvailabilityClaimEngine(
        steps = mutableListOf<AvailabilityClaimStep>(
            TieredAvailabilityClaimAdjuster(
                mutableListOf<Double>(6268.0, 9573.0),
                mutableListOf<Double>(1.05, 0.82),
            ),
            MinimumAvailabilityClaimAdjuster(44.0),
            RoundingAvailabilityClaimAdjuster(0.25),
            FlaggedAvailabilityClaimAdjuster(flags, "availability-claim-regional", 0.1),
            AvailabilityClaimBatchSizeOffsetAdjuster(1.56),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 67117.3,
    )
}
