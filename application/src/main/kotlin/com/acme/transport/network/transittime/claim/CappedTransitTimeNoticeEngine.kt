package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped transit time notice calculation. */
interface CappedTransitTimeNoticeModifier {
    fun describe(): String
    fun adjust(item: CappedTransitTimeNotice, value: Double): Double
}

data class CappedTransitTimeNoticeDistanceKmOffsetAdjuster(
    var weight: Double,
) : CappedTransitTimeNoticeModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: CappedTransitTimeNotice, value: Double): Double = value + item.distanceKm * weight
}

data class FlaggedCappedTransitTimeNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTransitTimeNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTransitTimeNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedTransitTimeNoticeAdjuster(var step: Double) : CappedTransitTimeNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedTransitTimeNotice, value: Double): Double = roundTo(value, step)
}

data class PercentCappedTransitTimeNoticeAdjuster(var percent: Double) : CappedTransitTimeNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedTransitTimeNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceCappedTransitTimeNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedTransitTimeNoticeModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: CappedTransitTimeNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedTransitTimeNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedTransitTimeNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedTransitTimeNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped transit time notice steps in order and keeps a trace of each. */
data class CappedTransitTimeNoticeEngine(
    var steps: MutableList<CappedTransitTimeNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedTransitTimeNotice): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedTransitTimeNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedTransitTimeNoticeEngine(flags: Registry): CappedTransitTimeNoticeEngine {
    return CappedTransitTimeNoticeEngine(
        steps = mutableListOf<CappedTransitTimeNoticeModifier>(
            CappedTransitTimeNoticeDistanceKmOffsetAdjuster(1.27),
            FlaggedCappedTransitTimeNoticeAdjuster(flags, "capped-transit-time-notice-rolling", -3.2),
            RoundingCappedTransitTimeNoticeAdjuster(1.0),
            PercentCappedTransitTimeNoticeAdjuster(2.3),
            SourceCappedTransitTimeNoticeAdjuster(mutableMapOf<String, Double>("north" to 0.87, "west" to 0.81)),
            TieredCappedTransitTimeNoticeAdjuster(
                mutableListOf<Double>(3227.0, 4031.0, 8722.0),
                mutableListOf<Double>(0.86, 0.97, 1.2),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 77599.5,
    )
}
