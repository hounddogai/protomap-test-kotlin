package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive fuel burn claim calculation. */
interface AdaptiveFuelBurnClaimStep {
    fun describe(): String
    fun adjust(item: AdaptiveFuelBurnClaim, value: Double): Double
}

data class FlaggedAdaptiveFuelBurnClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveFuelBurnClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveFuelBurnClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveFuelBurnClaimAdjuster(var minimum: Double) : AdaptiveFuelBurnClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveFuelBurnClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveFuelBurnClaimAdjuster(var step: Double) : AdaptiveFuelBurnClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveFuelBurnClaim, value: Double): Double = roundTo(value, step)
}

data class TieredAdaptiveFuelBurnClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveFuelBurnClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveFuelBurnClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveFuelBurnClaimAdjuster(var cap: Double) : AdaptiveFuelBurnClaimStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveFuelBurnClaim, value: Double): Double = minOf(value, cap)
}

data class LabelAdaptiveFuelBurnClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveFuelBurnClaimStep {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveFuelBurnClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive fuel burn claim steps in order and keeps a trace of each. */
data class AdaptiveFuelBurnClaimEngine(
    var steps: MutableList<AdaptiveFuelBurnClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveFuelBurnClaim): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveFuelBurnClaimStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveFuelBurnClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveFuelBurnClaimEngine(flags: Registry): AdaptiveFuelBurnClaimEngine {
    return AdaptiveFuelBurnClaimEngine(
        steps = mutableListOf<AdaptiveFuelBurnClaimStep>(
            FlaggedAdaptiveFuelBurnClaimAdjuster(flags, "adaptive-fuel-burn-claim-seasonal", 9.2),
            MinimumAdaptiveFuelBurnClaimAdjuster(32.3),
            RoundingAdaptiveFuelBurnClaimAdjuster(5.0),
            TieredAdaptiveFuelBurnClaimAdjuster(
                mutableListOf<Double>(2582.0, 2697.0, 3361.0, 3745.0),
                mutableListOf<Double>(0.85, 0.98, 1.11, 1.0),
            ),
            CapAdaptiveFuelBurnClaimAdjuster(956.0),
            LabelAdaptiveFuelBurnClaimAdjuster(mutableMapOf<String, Double>("west" to 1.2)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 66833.3,
    )
}
