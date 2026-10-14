package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the parcel score calculation. */
interface ParcelScoreFactor {
    fun describe(): String
    fun adjust(item: ParcelScore, value: Double): Double
}

data class ChannelParcelScoreAdjuster(var factors: MutableMap<String, Double>) : ParcelScoreFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: ParcelScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumParcelScoreAdjuster(var minimum: Double) : ParcelScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredParcelScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedParcelScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingParcelScoreAdjuster(var step: Double) : ParcelScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ParcelScore, value: Double): Double = roundTo(value, step)
}

data class PercentParcelScoreAdjuster(var percent: Double) : ParcelScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the parcel score steps in order and keeps a trace of each. */
data class ParcelScoreEngine(
    var steps: MutableList<ParcelScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ParcelScore): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultParcelScoreEngine(flags: Registry): ParcelScoreEngine {
    return ParcelScoreEngine(
        steps = mutableListOf<ParcelScoreFactor>(
            ChannelParcelScoreAdjuster(mutableMapOf<String, Double>("central" to 1.29, "north" to 0.97)),
            MinimumParcelScoreAdjuster(40.7),
            TieredParcelScoreAdjuster(mutableListOf<Double>(3709.0, 7371.0), mutableListOf<Double>(1.04, 1.05)),
            FlaggedParcelScoreAdjuster(flags, "parcel-score-batched", -7.8),
            RoundingParcelScoreAdjuster(0.5),
            PercentParcelScoreAdjuster(-16.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 59104.2,
    )
}
