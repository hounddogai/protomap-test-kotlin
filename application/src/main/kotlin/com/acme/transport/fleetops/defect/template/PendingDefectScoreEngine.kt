package com.acme.transport.fleetops.defect.template

/** One step of the pending defect score calculation. */
interface PendingDefectScoreAdjuster {
    fun describe(): String
    fun adjust(item: PendingDefectScore, value: Double): Double
}

data class TitlePendingDefectScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDefectScoreAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingDefectScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingDefectScoreAdjuster(var percent: Double) : PendingDefectScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDefectScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingDefectScoreAdjuster(var minimum: Double) : PendingDefectScoreAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDefectScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending defect score steps in order and keeps a trace of each. */
data class PendingDefectScoreEngine(
    var steps: MutableList<PendingDefectScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingDefectScore): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingDefectScoreAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingDefectScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingDefectScoreEngine(): PendingDefectScoreEngine {
    return PendingDefectScoreEngine(
        steps = mutableListOf<PendingDefectScoreAdjuster>(
            TitlePendingDefectScoreAdjuster(
                mutableMapOf<String, Double>("online" to 0.89, "west" to 0.78, "retail" to 1.12),
            ),
            PercentPendingDefectScoreAdjuster(9.9),
            MinimumPendingDefectScoreAdjuster(18.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 29815.6,
    )
}
