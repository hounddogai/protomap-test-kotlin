package com.acme.transport.linehaul.backhaul.ledger

/** One step of the backhaul metric calculation. */
interface BackhaulMetricAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulMetric, value: Double): Double
}

data class TitleBackhaulMetricAdjuster(var factors: MutableMap<String, Double>) : BackhaulMetricAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: BackhaulMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BackhaulMetricAmountOffsetAdjuster(var weight: Double) : BackhaulMetricAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: BackhaulMetric, value: Double): Double = value + item.amount * weight
}

data class CapBackhaulMetricAdjuster(var cap: Double) : BackhaulMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the backhaul metric steps in order and keeps a trace of each. */
data class BackhaulMetricEngine(
    var steps: MutableList<BackhaulMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BackhaulMetric): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulMetricAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBackhaulMetricEngine(): BackhaulMetricEngine {
    return BackhaulMetricEngine(
        steps = mutableListOf<BackhaulMetricAdjuster>(
            TitleBackhaulMetricAdjuster(mutableMapOf<String, Double>("north" to 1.23, "retail" to 1.34)),
            BackhaulMetricAmountOffsetAdjuster(1.43),
            CapBackhaulMetricAdjuster(38508.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 98324.9,
    )
}
