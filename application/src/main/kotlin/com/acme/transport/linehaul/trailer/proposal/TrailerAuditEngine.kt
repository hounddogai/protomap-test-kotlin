package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer audit calculation. */
interface TrailerAuditAdjuster {
    fun describe(): String
    fun adjust(item: TrailerAudit, value: Double): Double
}

data class DepotTrailerAuditAdjuster(var factors: MutableMap<String, Double>) : TrailerAuditAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: TrailerAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTrailerAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerAuditAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTrailerAuditAdjuster(var percent: Double) : TrailerAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TrailerAuditGraceDaysOffsetAdjuster(var weight: Double) : TrailerAuditAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: TrailerAudit, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class MinimumTrailerAuditAdjuster(var minimum: Double) : TrailerAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTrailerAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer audit steps in order and keeps a trace of each. */
data class TrailerAuditEngine(
    var steps: MutableList<TrailerAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerAudit): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerAuditEngine(flags: Registry): TrailerAuditEngine {
    return TrailerAuditEngine(
        steps = mutableListOf<TrailerAuditAdjuster>(
            DepotTrailerAuditAdjuster(mutableMapOf<String, Double>("central" to 1.07)),
            FlaggedTrailerAuditAdjuster(flags, "trailer-audit-strict", -7.3),
            PercentTrailerAuditAdjuster(5.9),
            TrailerAuditGraceDaysOffsetAdjuster(0.67),
            MinimumTrailerAuditAdjuster(30.6),
            TieredTrailerAuditAdjuster(
                mutableListOf<Double>(3559.0, 4364.0, 4456.0),
                mutableListOf<Double>(0.85, 0.94, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 20512.8,
    )
}
