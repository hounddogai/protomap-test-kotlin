package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy node metric calculation. */
interface LegacyNodeMetricFactor {
    fun describe(): String
    fun adjust(item: LegacyNodeMetric, value: Double): Double
}

data class LegacyNodeMetricQuantityOffsetAdjuster(var weight: Double) : LegacyNodeMetricFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LegacyNodeMetric, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class FlaggedLegacyNodeMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyNodeMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyNodeMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelLegacyNodeMetricAdjuster(var factors: MutableMap<String, Double>) : LegacyNodeMetricFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: LegacyNodeMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyNodeMetricAdjuster(var percent: Double) : LegacyNodeMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyNodeMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy node metric steps in order and keeps a trace of each. */
data class LegacyNodeMetricEngine(
    var steps: MutableList<LegacyNodeMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyNodeMetric): Double {
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

    fun evaluateAll(items: MutableList<LegacyNodeMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyNodeMetricEngine(flags: Registry): LegacyNodeMetricEngine {
    return LegacyNodeMetricEngine(
        steps = mutableListOf<LegacyNodeMetricFactor>(
            LegacyNodeMetricQuantityOffsetAdjuster(1.53),
            FlaggedLegacyNodeMetricAdjuster(flags, "legacy-node-metric-manual", 5.1),
            LabelLegacyNodeMetricAdjuster(mutableMapOf<String, Double>("online" to 1.16, "east" to 0.72)),
            PercentLegacyNodeMetricAdjuster(2.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 96575.4,
    )
}
