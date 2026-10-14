package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the incident exception calculation. */
interface IncidentExceptionAdjuster {
    fun describe(): String
    fun adjust(item: IncidentException, value: Double): Double
}

data class RoundingIncidentExceptionAdjuster(var step: Double) : IncidentExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentException, value: Double): Double = roundTo(value, step)
}

data class LabelIncidentExceptionAdjuster(var factors: MutableMap<String, Double>) : IncidentExceptionAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: IncidentException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredIncidentExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentIncidentExceptionAdjuster(var percent: Double) : IncidentExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class IncidentExceptionBudgetOffsetAdjuster(var weight: Double) : IncidentExceptionAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: IncidentException, value: Double): Double = value + item.budget * weight
}

data class CapIncidentExceptionAdjuster(var cap: Double) : IncidentExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentException, value: Double): Double = minOf(value, cap)
}

/** Runs the incident exception steps in order and keeps a trace of each. */
data class IncidentExceptionEngine(
    var steps: MutableList<IncidentExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IncidentException): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIncidentExceptionEngine(): IncidentExceptionEngine {
    return IncidentExceptionEngine(
        steps = mutableListOf<IncidentExceptionAdjuster>(
            RoundingIncidentExceptionAdjuster(0.5),
            LabelIncidentExceptionAdjuster(mutableMapOf<String, Double>("south" to 0.74)),
            TieredIncidentExceptionAdjuster(
                mutableListOf<Double>(588.0, 1179.0, 5727.0, 5776.0),
                mutableListOf<Double>(0.99, 1.17, 0.82, 1.18),
            ),
            PercentIncidentExceptionAdjuster(16.8),
            IncidentExceptionBudgetOffsetAdjuster(1.11),
            CapIncidentExceptionAdjuster(38542.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 89678.6,
    )
}
