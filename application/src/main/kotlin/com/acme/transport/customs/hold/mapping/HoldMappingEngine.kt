package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hold mapping calculation. */
interface HoldMappingFactor {
    fun describe(): String
    fun adjust(item: HoldMapping, value: Double): Double
}

data class PercentHoldMappingAdjuster(var percent: Double) : HoldMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class HoldMappingGraceDaysOffsetAdjuster(var weight: Double) : HoldMappingFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: HoldMapping, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class CapHoldMappingAdjuster(var cap: Double) : HoldMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the hold mapping steps in order and keeps a trace of each. */
data class HoldMappingEngine(
    var steps: MutableList<HoldMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HoldMapping): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HoldMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHoldMappingEngine(): HoldMappingEngine {
    return HoldMappingEngine(
        steps = mutableListOf<HoldMappingFactor>(
            PercentHoldMappingAdjuster(2.4),
            HoldMappingGraceDaysOffsetAdjuster(1.16),
            CapHoldMappingAdjuster(28813.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 93065.1,
    )
}
