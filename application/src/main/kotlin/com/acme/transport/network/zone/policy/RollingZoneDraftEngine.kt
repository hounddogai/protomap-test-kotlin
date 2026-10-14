package com.acme.transport.network.zone.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling zone draft calculation. */
interface RollingZoneDraftModifier {
    fun describe(): String
    fun adjust(item: RollingZoneDraft, value: Double): Double
}

data class RoundingRollingZoneDraftAdjuster(var step: Double) : RollingZoneDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingZoneDraft, value: Double): Double = roundTo(value, step)
}

data class TieredRollingZoneDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingZoneDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingZoneDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRollingZoneDraftAdjuster(var percent: Double) : RollingZoneDraftModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingZoneDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRollingZoneDraftAdjuster(var minimum: Double) : RollingZoneDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingZoneDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRollingZoneDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingZoneDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingZoneDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RollingZoneDraftExpiresAtOffsetAdjuster(var weight: Double) : RollingZoneDraftModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: RollingZoneDraft, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

/** Runs the rolling zone draft steps in order and keeps a trace of each. */
data class RollingZoneDraftEngine(
    var steps: MutableList<RollingZoneDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingZoneDraft): Double {
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

    fun evaluateAll(items: MutableList<RollingZoneDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingZoneDraftEngine(flags: Registry): RollingZoneDraftEngine {
    return RollingZoneDraftEngine(
        steps = mutableListOf<RollingZoneDraftModifier>(
            RoundingRollingZoneDraftAdjuster(1.0),
            TieredRollingZoneDraftAdjuster(
                mutableListOf<Double>(2161.0, 4963.0),
                mutableListOf<Double>(0.98, 0.83),
            ),
            PercentRollingZoneDraftAdjuster(1.1),
            MinimumRollingZoneDraftAdjuster(1.0),
            FlaggedRollingZoneDraftAdjuster(flags, "rolling-zone-draft-primary", -9.7),
            RollingZoneDraftExpiresAtOffsetAdjuster(1.04),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 7177.6,
    )
}
