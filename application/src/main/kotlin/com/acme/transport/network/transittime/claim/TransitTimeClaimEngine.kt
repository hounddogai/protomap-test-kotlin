package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the transit time claim calculation. */
interface TransitTimeClaimAdjuster {
    fun describe(): String
    fun adjust(item: TransitTimeClaim, value: Double): Double
}

data class FlaggedTransitTimeClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameTransitTimeClaimAdjuster(var factors: MutableMap<String, Double>) : TransitTimeClaimAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TransitTimeClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTransitTimeClaimAdjuster(var step: Double) : TransitTimeClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeClaim, value: Double): Double = roundTo(value, step)
}

data class MinimumTransitTimeClaimAdjuster(var minimum: Double) : TransitTimeClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTransitTimeClaimAdjuster(var percent: Double) : TransitTimeClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the transit time claim steps in order and keeps a trace of each. */
data class TransitTimeClaimEngine(
    var steps: MutableList<TransitTimeClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TransitTimeClaim): Double {
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

    fun evaluateAll(items: MutableList<TransitTimeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTransitTimeClaimEngine(flags: Registry): TransitTimeClaimEngine {
    return TransitTimeClaimEngine(
        steps = mutableListOf<TransitTimeClaimAdjuster>(
            FlaggedTransitTimeClaimAdjuster(flags, "transit-time-claim-batched", -2.8),
            NameTransitTimeClaimAdjuster(mutableMapOf<String, Double>("east" to 1.16, "online" to 0.99)),
            RoundingTransitTimeClaimAdjuster(0.25),
            MinimumTransitTimeClaimAdjuster(47.5),
            PercentTransitTimeClaimAdjuster(-3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 70478.4,
    )
}
