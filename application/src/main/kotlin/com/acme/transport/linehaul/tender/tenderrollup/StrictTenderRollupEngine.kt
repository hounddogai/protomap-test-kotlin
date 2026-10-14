package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict tender rollup calculation. */
interface StrictTenderRollupModifier {
    fun describe(): String
    fun adjust(item: StrictTenderRollup, value: Double): Double
}

data class LabelStrictTenderRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictTenderRollupModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictTenderRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictTenderRollupAdjuster(var percent: Double) : StrictTenderRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTenderRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictTenderRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTenderRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTenderRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingStrictTenderRollupAdjuster(var step: Double) : StrictTenderRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTenderRollup, value: Double): Double = roundTo(value, step)
}

/** Runs the strict tender rollup steps in order and keeps a trace of each. */
data class StrictTenderRollupEngine(
    var steps: MutableList<StrictTenderRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictTenderRollup): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictTenderRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictTenderRollupEngine(): StrictTenderRollupEngine {
    return StrictTenderRollupEngine(
        steps = mutableListOf<StrictTenderRollupModifier>(
            LabelStrictTenderRollupAdjuster(mutableMapOf<String, Double>("online" to 1.18, "retail" to 0.78)),
            PercentStrictTenderRollupAdjuster(-3.2),
            TieredStrictTenderRollupAdjuster(
                mutableListOf<Double>(2137.0, 3925.0, 4886.0),
                mutableListOf<Double>(0.86, 1.05, 0.98),
            ),
            RoundingStrictTenderRollupAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 60621.7,
    )
}
