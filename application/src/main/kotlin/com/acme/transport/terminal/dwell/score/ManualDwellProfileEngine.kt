package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual dwell profile calculation. */
interface ManualDwellProfileModifier {
    fun describe(): String
    fun adjust(item: ManualDwellProfile, value: Double): Double
}

data class TieredManualDwellProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDwellProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDwellProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ManualDwellProfileFloorValueOffsetAdjuster(var weight: Double) : ManualDwellProfileModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: ManualDwellProfile, value: Double): Double = value + item.floorValue * weight
}

data class MinimumManualDwellProfileAdjuster(var minimum: Double) : ManualDwellProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDwellProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentManualDwellProfileAdjuster(var percent: Double) : ManualDwellProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDwellProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualDwellProfileAdjuster(var step: Double) : ManualDwellProfileModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDwellProfile, value: Double): Double = roundTo(value, step)
}

data class CapManualDwellProfileAdjuster(var cap: Double) : ManualDwellProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDwellProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the manual dwell profile steps in order and keeps a trace of each. */
data class ManualDwellProfileEngine(
    var steps: MutableList<ManualDwellProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualDwellProfile): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualDwellProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualDwellProfileEngine(): ManualDwellProfileEngine {
    return ManualDwellProfileEngine(
        steps = mutableListOf<ManualDwellProfileModifier>(
            TieredManualDwellProfileAdjuster(
                mutableListOf<Double>(7592.0, 8546.0),
                mutableListOf<Double>(0.86, 1.06),
            ),
            ManualDwellProfileFloorValueOffsetAdjuster(1.85),
            MinimumManualDwellProfileAdjuster(7.5),
            PercentManualDwellProfileAdjuster(-24.1),
            RoundingManualDwellProfileAdjuster(0.1),
            CapManualDwellProfileAdjuster(4204.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 53836.6,
    )
}
