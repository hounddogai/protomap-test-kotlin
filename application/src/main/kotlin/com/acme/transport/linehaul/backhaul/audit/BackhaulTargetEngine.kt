package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul target calculation. */
interface BackhaulTargetModifier {
    fun describe(): String
    fun adjust(item: BackhaulTarget, value: Double): Double
}

data class LabelBackhaulTargetAdjuster(var factors: MutableMap<String, Double>) : BackhaulTargetModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: BackhaulTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBackhaulTargetAdjuster(var cap: Double) : BackhaulTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulTarget, value: Double): Double = minOf(value, cap)
}

data class TieredBackhaulTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulTargetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBackhaulTargetAdjuster(var percent: Double) : BackhaulTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingBackhaulTargetAdjuster(var step: Double) : BackhaulTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulTarget, value: Double): Double = roundTo(value, step)
}

/** Runs the backhaul target steps in order and keeps a trace of each. */
data class BackhaulTargetEngine(
    var steps: MutableList<BackhaulTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BackhaulTarget): Double {
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

    fun evaluateAll(items: MutableList<BackhaulTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBackhaulTargetEngine(): BackhaulTargetEngine {
    return BackhaulTargetEngine(
        steps = mutableListOf<BackhaulTargetModifier>(
            LabelBackhaulTargetAdjuster(mutableMapOf<String, Double>("south" to 1.12, "north" to 0.95)),
            CapBackhaulTargetAdjuster(35876.0),
            TieredBackhaulTargetAdjuster(
                mutableListOf<Double>(259.0, 3269.0, 4670.0, 8958.0),
                mutableListOf<Double>(0.92, 1.06, 0.93, 1.02),
            ),
            PercentBackhaulTargetAdjuster(14.4),
            RoundingBackhaulTargetAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 28027.2,
    )
}
