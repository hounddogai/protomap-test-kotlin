package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock agreement calculation. */
interface CrossDockAgreementFactor {
    fun describe(): String
    fun adjust(item: CrossDockAgreement, value: Double): Double
}

data class FlaggedCrossDockAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCrossDockAgreementAdjuster(var step: Double) : CrossDockAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumCrossDockAgreementAdjuster(var minimum: Double) : CrossDockAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCrossDockAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCrossDockAgreementAdjuster(var percent: Double) : CrossDockAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the cross dock agreement steps in order and keeps a trace of each. */
data class CrossDockAgreementEngine(
    var steps: MutableList<CrossDockAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CrossDockAgreement): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCrossDockAgreementEngine(flags: Registry): CrossDockAgreementEngine {
    return CrossDockAgreementEngine(
        steps = mutableListOf<CrossDockAgreementFactor>(
            FlaggedCrossDockAgreementAdjuster(flags, "cross-dock-agreement-rolling", 1.6),
            RoundingCrossDockAgreementAdjuster(0.05),
            MinimumCrossDockAgreementAdjuster(46.3),
            TieredCrossDockAgreementAdjuster(
                mutableListOf<Double>(1277.0, 2611.0, 6303.0, 9711.0),
                mutableListOf<Double>(0.87, 0.86, 1.09, 0.89),
            ),
            PercentCrossDockAgreementAdjuster(-24.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 88776.6,
    )
}
