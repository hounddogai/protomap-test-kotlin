package com.acme.transport.linehaul.relay.batch

/** One step of the relay segment calculation. */
interface RelaySegmentAdjuster {
    fun describe(): String
    fun adjust(item: RelaySegment, value: Double): Double
}

data class RelaySegmentMarginOffsetAdjuster(var weight: Double) : RelaySegmentAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: RelaySegment, value: Double): Double = value + item.margin * weight
}

data class PercentRelaySegmentAdjuster(var percent: Double) : RelaySegmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelaySegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentRelaySegmentAdjuster(var factors: MutableMap<String, Double>) : RelaySegmentAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: RelaySegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the relay segment steps in order and keeps a trace of each. */
data class RelaySegmentEngine(
    var steps: MutableList<RelaySegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelaySegment): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelaySegmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RelaySegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelaySegmentEngine(): RelaySegmentEngine {
    return RelaySegmentEngine(
        steps = mutableListOf<RelaySegmentAdjuster>(
            RelaySegmentMarginOffsetAdjuster(0.14),
            PercentRelaySegmentAdjuster(24.1),
            SegmentRelaySegmentAdjuster(
                mutableMapOf<String, Double>("north" to 1.13, "retail" to 1.17, "west" to 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 76827.0,
    )
}
