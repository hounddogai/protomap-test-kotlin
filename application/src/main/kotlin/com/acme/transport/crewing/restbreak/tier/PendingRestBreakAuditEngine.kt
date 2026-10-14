package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending rest break audit calculation. */
interface PendingRestBreakAuditAdjuster {
    fun describe(): String
    fun adjust(item: PendingRestBreakAudit, value: Double): Double
}

data class TitlePendingRestBreakAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRestBreakAuditAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingRestBreakAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPendingRestBreakAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingRestBreakAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingRestBreakAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPendingRestBreakAuditAdjuster(var step: Double) : PendingRestBreakAuditAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRestBreakAudit, value: Double): Double = roundTo(value, step)
}

/** Runs the pending rest break audit steps in order and keeps a trace of each. */
data class PendingRestBreakAuditEngine(
    var steps: MutableList<PendingRestBreakAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingRestBreakAudit): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingRestBreakAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingRestBreakAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingRestBreakAuditEngine(): PendingRestBreakAuditEngine {
    return PendingRestBreakAuditEngine(
        steps = mutableListOf<PendingRestBreakAuditAdjuster>(
            TitlePendingRestBreakAuditAdjuster(mutableMapOf<String, Double>("online" to 1.14, "south" to 0.86)),
            TieredPendingRestBreakAuditAdjuster(
                mutableListOf<Double>(2377.0, 4634.0, 5973.0, 6033.0),
                mutableListOf<Double>(0.88, 1.17, 1.02, 1.03),
            ),
            RoundingPendingRestBreakAuditAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 12727.5,
    )
}
