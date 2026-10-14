package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rate card cycle calculation. */
interface RateCardCycleStep {
    fun describe(): String
    fun adjust(item: RateCardCycle, value: Double): Double
}

data class FlaggedRateCardCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRateCardCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRateCardCycleAdjuster(var minimum: Double) : RateCardCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RateCardCycleCostOffsetAdjuster(var weight: Double) : RateCardCycleStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: RateCardCycle, value: Double): Double = value + item.cost * weight
}

data class LabelRateCardCycleAdjuster(var factors: MutableMap<String, Double>) : RateCardCycleStep {
    override fun describe(): String = "by label"

    override fun adjust(item: RateCardCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRateCardCycleAdjuster(var step: Double) : RateCardCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the rate card cycle steps in order and keeps a trace of each. */
data class RateCardCycleEngine(
    var steps: MutableList<RateCardCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RateCardCycle): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRateCardCycleEngine(flags: Registry): RateCardCycleEngine {
    return RateCardCycleEngine(
        steps = mutableListOf<RateCardCycleStep>(
            FlaggedRateCardCycleAdjuster(flags, "rate-card-cycle-tiered", -6.2),
            TieredRateCardCycleAdjuster(
                mutableListOf<Double>(1012.0, 4168.0, 9593.0, 9714.0),
                mutableListOf<Double>(0.82, 1.05, 1.0, 0.96),
            ),
            MinimumRateCardCycleAdjuster(22.3),
            RateCardCycleCostOffsetAdjuster(1.99),
            LabelRateCardCycleAdjuster(mutableMapOf<String, Double>("retail" to 1.21, "north" to 1.35)),
            RoundingRateCardCycleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 9023.1,
    )
}
