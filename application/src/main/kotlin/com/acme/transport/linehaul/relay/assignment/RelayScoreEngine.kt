package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the relay score calculation. */
interface RelayScoreFactor {
    fun describe(): String
    fun adjust(item: RelayScore, value: Double): Double
}

data class TieredRelayScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentRelayScoreAdjuster(var factors: MutableMap<String, Double>) : RelayScoreFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: RelayScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRelayScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRelayScoreAdjuster(var percent: Double) : RelayScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRelayScoreAdjuster(var step: Double) : RelayScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayScore, value: Double): Double = roundTo(value, step)
}

data class RelayScoreDiscountOffsetAdjuster(var weight: Double) : RelayScoreFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: RelayScore, value: Double): Double = value + item.discount * weight
}

/** Runs the relay score steps in order and keeps a trace of each. */
data class RelayScoreEngine(
    var steps: MutableList<RelayScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayScore): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RelayScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayScoreEngine(flags: Registry): RelayScoreEngine {
    return RelayScoreEngine(
        steps = mutableListOf<RelayScoreFactor>(
            TieredRelayScoreAdjuster(mutableListOf<Double>(617.0, 4919.0), mutableListOf<Double>(1.18, 1.02)),
            SegmentRelayScoreAdjuster(
                mutableMapOf<String, Double>("west" to 0.95, "south" to 0.79, "north" to 1.05),
            ),
            FlaggedRelayScoreAdjuster(flags, "relay-score-lenient", 9.7),
            PercentRelayScoreAdjuster(20.4),
            RoundingRelayScoreAdjuster(1.0),
            RelayScoreDiscountOffsetAdjuster(0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 62743.9,
    )
}
