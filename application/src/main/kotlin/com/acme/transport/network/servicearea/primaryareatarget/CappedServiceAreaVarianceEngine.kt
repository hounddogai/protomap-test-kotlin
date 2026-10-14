package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped service area variance calculation. */
interface CappedServiceAreaVarianceStep {
    fun describe(): String
    fun adjust(item: CappedServiceAreaVariance, value: Double): Double
}

data class CapCappedServiceAreaVarianceAdjuster(var cap: Double) : CappedServiceAreaVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedServiceAreaVariance, value: Double): Double = minOf(value, cap)
}

data class PercentCappedServiceAreaVarianceAdjuster(var percent: Double) : CappedServiceAreaVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedServiceAreaVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedServiceAreaVarianceAdjuster(var step: Double) : CappedServiceAreaVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedServiceAreaVariance, value: Double): Double = roundTo(value, step)
}

data class SourceCappedServiceAreaVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedServiceAreaVarianceStep {
    override fun describe(): String = "by source"

    override fun adjust(item: CappedServiceAreaVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped service area variance steps in order and keeps a trace of each. */
data class CappedServiceAreaVarianceEngine(
    var steps: MutableList<CappedServiceAreaVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedServiceAreaVariance): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedServiceAreaVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedServiceAreaVarianceEngine(): CappedServiceAreaVarianceEngine {
    return CappedServiceAreaVarianceEngine(
        steps = mutableListOf<CappedServiceAreaVarianceStep>(
            CapCappedServiceAreaVarianceAdjuster(40918.0),
            PercentCappedServiceAreaVarianceAdjuster(11.4),
            RoundingCappedServiceAreaVarianceAdjuster(5.0),
            SourceCappedServiceAreaVarianceAdjuster(
                mutableMapOf<String, Double>("online" to 1.24, "north" to 1.34, "retail" to 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 57548.8,
    )
}
