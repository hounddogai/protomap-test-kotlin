package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling tender group calculation. */
interface RollingTenderGroupFactor {
    fun describe(): String
    fun adjust(item: RollingTenderGroup, value: Double): Double
}

data class TieredRollingTenderGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingTenderGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingTenderGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRollingTenderGroupAdjuster(var percent: Double) : RollingTenderGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingTenderGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingTenderGroupAdjuster(var step: Double) : RollingTenderGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingTenderGroup, value: Double): Double = roundTo(value, step)
}

data class CapRollingTenderGroupAdjuster(var cap: Double) : RollingTenderGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTenderGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling tender group steps in order and keeps a trace of each. */
data class RollingTenderGroupEngine(
    var steps: MutableList<RollingTenderGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingTenderGroup): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingTenderGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingTenderGroupEngine(): RollingTenderGroupEngine {
    return RollingTenderGroupEngine(
        steps = mutableListOf<RollingTenderGroupFactor>(
            TieredRollingTenderGroupAdjuster(
                mutableListOf<Double>(6190.0, 8259.0, 8695.0, 9085.0),
                mutableListOf<Double>(0.84, 0.91, 0.86, 1.05),
            ),
            PercentRollingTenderGroupAdjuster(18.2),
            RoundingRollingTenderGroupAdjuster(0.1),
            CapRollingTenderGroupAdjuster(33620.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 96133.9,
    )
}
