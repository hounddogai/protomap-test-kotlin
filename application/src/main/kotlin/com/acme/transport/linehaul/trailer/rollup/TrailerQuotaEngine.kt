package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer quota calculation. */
interface TrailerQuotaAdjuster {
    fun describe(): String
    fun adjust(item: TrailerQuota, value: Double): Double
}

data class TitleTrailerQuotaAdjuster(var factors: MutableMap<String, Double>) : TrailerQuotaAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: TrailerQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TrailerQuotaPriorityOffsetAdjuster(var weight: Double) : TrailerQuotaAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: TrailerQuota, value: Double): Double = value + item.priority.toDouble() * weight
}

data class CapTrailerQuotaAdjuster(var cap: Double) : TrailerQuotaAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerQuota, value: Double): Double = minOf(value, cap)
}

data class RoundingTrailerQuotaAdjuster(var step: Double) : TrailerQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerQuota, value: Double): Double = roundTo(value, step)
}

data class FlaggedTrailerQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTrailerQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer quota steps in order and keeps a trace of each. */
data class TrailerQuotaEngine(
    var steps: MutableList<TrailerQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerQuota): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerQuotaEngine(flags: Registry): TrailerQuotaEngine {
    return TrailerQuotaEngine(
        steps = mutableListOf<TrailerQuotaAdjuster>(
            TitleTrailerQuotaAdjuster(mutableMapOf<String, Double>("central" to 1.3)),
            TrailerQuotaPriorityOffsetAdjuster(0.69),
            CapTrailerQuotaAdjuster(22850.0),
            RoundingTrailerQuotaAdjuster(0.5),
            FlaggedTrailerQuotaAdjuster(flags, "trailer-quota-tiered", -7.0),
            TieredTrailerQuotaAdjuster(
                mutableListOf<Double>(2865.0, 6019.0, 8972.0),
                mutableListOf<Double>(1.09, 1.12, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 51353.9,
    )
}
