package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer yard notice calculation. */
interface TrailerYardNoticeModifier {
    fun describe(): String
    fun adjust(item: TrailerYardNotice, value: Double): Double
}

data class FlaggedTrailerYardNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTrailerYardNoticeAdjuster(var step: Double) : TrailerYardNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardNotice, value: Double): Double = roundTo(value, step)
}

data class DepotTrailerYardNoticeAdjuster(var factors: MutableMap<String, Double>) : TrailerYardNoticeModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: TrailerYardNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTrailerYardNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer yard notice steps in order and keeps a trace of each. */
data class TrailerYardNoticeEngine(
    var steps: MutableList<TrailerYardNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardNotice): Double {
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

    fun evaluateAll(items: MutableList<TrailerYardNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardNoticeEngine(flags: Registry): TrailerYardNoticeEngine {
    return TrailerYardNoticeEngine(
        steps = mutableListOf<TrailerYardNoticeModifier>(
            FlaggedTrailerYardNoticeAdjuster(flags, "trailer-yard-notice-pending", 1.0),
            RoundingTrailerYardNoticeAdjuster(5.0),
            DepotTrailerYardNoticeAdjuster(mutableMapOf<String, Double>("east" to 1.3)),
            TieredTrailerYardNoticeAdjuster(
                mutableListOf<Double>(3039.0, 5971.0, 6735.0),
                mutableListOf<Double>(1.18, 1.05, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 16055.7,
    )
}
