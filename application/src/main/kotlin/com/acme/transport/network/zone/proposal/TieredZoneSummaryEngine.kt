package com.acme.transport.network.zone.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered zone summary calculation. */
interface TieredZoneSummaryAdjuster {
    fun describe(): String
    fun adjust(item: TieredZoneSummary, value: Double): Double
}

data class TieredTieredZoneSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredZoneSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredZoneSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTieredZoneSummaryAdjuster(var cap: Double) : TieredZoneSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredZoneSummary, value: Double): Double = minOf(value, cap)
}

data class SourceTieredZoneSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredZoneSummaryAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: TieredZoneSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredZoneSummaryAdjuster(var step: Double) : TieredZoneSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredZoneSummary, value: Double): Double = roundTo(value, step)
}

data class FlaggedTieredZoneSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredZoneSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredZoneSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredZoneSummaryAttemptsOffsetAdjuster(var weight: Double) : TieredZoneSummaryAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: TieredZoneSummary, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the tiered zone summary steps in order and keeps a trace of each. */
data class TieredZoneSummaryEngine(
    var steps: MutableList<TieredZoneSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredZoneSummary): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredZoneSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredZoneSummaryEngine(flags: Registry): TieredZoneSummaryEngine {
    return TieredZoneSummaryEngine(
        steps = mutableListOf<TieredZoneSummaryAdjuster>(
            TieredTieredZoneSummaryAdjuster(
                mutableListOf<Double>(557.0, 6957.0, 7930.0),
                mutableListOf<Double>(1.0, 1.17, 0.83),
            ),
            CapTieredZoneSummaryAdjuster(9816.0),
            SourceTieredZoneSummaryAdjuster(
                mutableMapOf<String, Double>("retail" to 0.88, "east" to 1.33, "south" to 0.88),
            ),
            RoundingTieredZoneSummaryAdjuster(5.0),
            FlaggedTieredZoneSummaryAdjuster(flags, "tiered-zone-summary-pending", 0.8),
            TieredZoneSummaryAttemptsOffsetAdjuster(1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 80031.7,
    )
}
