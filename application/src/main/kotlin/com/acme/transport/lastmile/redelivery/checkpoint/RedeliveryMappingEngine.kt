package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the redelivery mapping calculation. */
interface RedeliveryMappingStep {
    fun describe(): String
    fun adjust(item: RedeliveryMapping, value: Double): Double
}

data class CapRedeliveryMappingAdjuster(var cap: Double) : RedeliveryMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryMapping, value: Double): Double = minOf(value, cap)
}

data class UnitRedeliveryMappingAdjuster(var factors: MutableMap<String, Double>) : RedeliveryMappingStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: RedeliveryMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRedeliveryMappingAdjuster(var step: Double) : RedeliveryMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryMapping, value: Double): Double = roundTo(value, step)
}

data class PercentRedeliveryMappingAdjuster(var percent: Double) : RedeliveryMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRedeliveryMappingAdjuster(var minimum: Double) : RedeliveryMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RedeliveryMappingMaximumOffsetAdjuster(var weight: Double) : RedeliveryMappingStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: RedeliveryMapping, value: Double): Double = value + item.maximum * weight
}

/** Runs the redelivery mapping steps in order and keeps a trace of each. */
data class RedeliveryMappingEngine(
    var steps: MutableList<RedeliveryMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RedeliveryMapping): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RedeliveryMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRedeliveryMappingEngine(): RedeliveryMappingEngine {
    return RedeliveryMappingEngine(
        steps = mutableListOf<RedeliveryMappingStep>(
            CapRedeliveryMappingAdjuster(6581.0),
            UnitRedeliveryMappingAdjuster(mutableMapOf<String, Double>("online" to 0.72, "retail" to 1.37)),
            RoundingRedeliveryMappingAdjuster(0.25),
            PercentRedeliveryMappingAdjuster(2.9),
            MinimumRedeliveryMappingAdjuster(20.3),
            RedeliveryMappingMaximumOffsetAdjuster(0.43),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 39359.9,
    )
}
