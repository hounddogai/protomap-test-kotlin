package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tire tier calculation. */
interface TireTierModifier {
    fun describe(): String
    fun adjust(item: TireTier, value: Double): Double
}

data class PercentTireTierAdjuster(var percent: Double) : TireTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTireTierAdjuster(var minimum: Double) : TireTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTireTierAdjuster(var cap: Double) : TireTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireTier, value: Double): Double = minOf(value, cap)
}

data class TieredTireTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTireTierAdjuster(var step: Double) : TireTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireTier, value: Double): Double = roundTo(value, step)
}

data class NameTireTierAdjuster(var factors: MutableMap<String, Double>) : TireTierModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: TireTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tire tier steps in order and keeps a trace of each. */
data class TireTierEngine(
    var steps: MutableList<TireTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TireTier): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTireTierEngine(): TireTierEngine {
    return TireTierEngine(
        steps = mutableListOf<TireTierModifier>(
            PercentTireTierAdjuster(-17.8),
            MinimumTireTierAdjuster(31.0),
            CapTireTierAdjuster(21215.0),
            TieredTireTierAdjuster(
                mutableListOf<Double>(2257.0, 3654.0, 7077.0, 9566.0),
                mutableListOf<Double>(1.08, 1.04, 0.82, 0.94),
            ),
            RoundingTireTierAdjuster(0.5),
            NameTireTierAdjuster(mutableMapOf<String, Double>("north" to 0.71, "east" to 1.18, "retail" to 0.8)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 65167.5,
    )
}
