package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell mapping calculation. */
interface DwellMappingStep {
    fun describe(): String
    fun adjust(item: DwellMapping, value: Double): Double
}

data class DepotDwellMappingAdjuster(var factors: MutableMap<String, Double>) : DwellMappingStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: DwellMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDwellMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDwellMappingAdjuster(var percent: Double) : DwellMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDwellMappingAdjuster(var step: Double) : DwellMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellMapping, value: Double): Double = roundTo(value, step)
}

data class DwellMappingOdometerOffsetAdjuster(var weight: Double) : DwellMappingStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: DwellMapping, value: Double): Double = value + item.odometer.toDouble() * weight
}

/** Runs the dwell mapping steps in order and keeps a trace of each. */
data class DwellMappingEngine(
    var steps: MutableList<DwellMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DwellMapping): Double {
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

    fun evaluateAll(items: MutableList<DwellMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDwellMappingEngine(): DwellMappingEngine {
    return DwellMappingEngine(
        steps = mutableListOf<DwellMappingStep>(
            DepotDwellMappingAdjuster(
                mutableMapOf<String, Double>("south" to 1.09, "online" to 0.86, "retail" to 0.85),
            ),
            TieredDwellMappingAdjuster(
                mutableListOf<Double>(3858.0, 4355.0, 5295.0, 6505.0),
                mutableListOf<Double>(0.85, 0.93, 1.19, 0.81),
            ),
            PercentDwellMappingAdjuster(-0.5),
            RoundingDwellMappingAdjuster(0.05),
            DwellMappingOdometerOffsetAdjuster(1.07),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 23018.6,
    )
}
