package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted dock door profile calculation. */
interface WeightedDockDoorProfileModifier {
    fun describe(): String
    fun adjust(item: WeightedDockDoorProfile, value: Double): Double
}

data class CapWeightedDockDoorProfileAdjuster(var cap: Double) : WeightedDockDoorProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedDockDoorProfile, value: Double): Double = minOf(value, cap)
}

data class RoundingWeightedDockDoorProfileAdjuster(var step: Double) : WeightedDockDoorProfileModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDockDoorProfile, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedDockDoorProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDockDoorProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDockDoorProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredWeightedDockDoorProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedDockDoorProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedDockDoorProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentWeightedDockDoorProfileAdjuster(var percent: Double) : WeightedDockDoorProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedDockDoorProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted dock door profile steps in order and keeps a trace of each. */
data class WeightedDockDoorProfileEngine(
    var steps: MutableList<WeightedDockDoorProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedDockDoorProfile): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedDockDoorProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedDockDoorProfileEngine(flags: Registry): WeightedDockDoorProfileEngine {
    return WeightedDockDoorProfileEngine(
        steps = mutableListOf<WeightedDockDoorProfileModifier>(
            CapWeightedDockDoorProfileAdjuster(3669.0),
            RoundingWeightedDockDoorProfileAdjuster(0.1),
            FlaggedWeightedDockDoorProfileAdjuster(flags, "weighted-dock-door-profile-lenient", 7.3),
            TieredWeightedDockDoorProfileAdjuster(
                mutableListOf<Double>(590.0, 1068.0, 1705.0),
                mutableListOf<Double>(0.98, 0.91, 1.09),
            ),
            PercentWeightedDockDoorProfileAdjuster(21.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 32036.2,
    )
}
