package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.numbers.roundTo

/** One step of the dwell notice calculation. */
interface DwellNoticeStep {
    fun describe(): String
    fun adjust(item: DwellNotice, value: Double): Double
}

data class DepotDwellNoticeAdjuster(var factors: MutableMap<String, Double>) : DwellNoticeStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: DwellNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDwellNoticeAdjuster(var step: Double) : DwellNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellNotice, value: Double): Double = roundTo(value, step)
}

data class PercentDwellNoticeAdjuster(var percent: Double) : DwellNoticeStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDwellNoticeAdjuster(var minimum: Double) : DwellNoticeStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dwell notice steps in order and keeps a trace of each. */
data class DwellNoticeEngine(
    var steps: MutableList<DwellNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DwellNotice): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<DwellNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDwellNoticeEngine(): DwellNoticeEngine {
    return DwellNoticeEngine(
        steps = mutableListOf<DwellNoticeStep>(
            DepotDwellNoticeAdjuster(
                mutableMapOf<String, Double>("west" to 1.31, "south" to 0.71, "east" to 1.24),
            ),
            RoundingDwellNoticeAdjuster(1.0),
            PercentDwellNoticeAdjuster(19.8),
            MinimumDwellNoticeAdjuster(18.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 36234.7,
    )
}
