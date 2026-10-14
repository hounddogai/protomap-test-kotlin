package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered coaching audit calculation. */
interface TieredCoachingAuditAdjuster {
    fun describe(): String
    fun adjust(item: TieredCoachingAudit, value: Double): Double
}

data class CapTieredCoachingAuditAdjuster(var cap: Double) : TieredCoachingAuditAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredCoachingAudit, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredCoachingAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCoachingAuditAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCoachingAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTieredCoachingAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredCoachingAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredCoachingAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTieredCoachingAuditAdjuster(var minimum: Double) : TieredCoachingAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredCoachingAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCoachingAuditEndsAtOffsetAdjuster(var weight: Double) : TieredCoachingAuditAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: TieredCoachingAudit, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the tiered coaching audit steps in order and keeps a trace of each. */
data class TieredCoachingAuditEngine(
    var steps: MutableList<TieredCoachingAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredCoachingAudit): Double {
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

    fun evaluateAll(items: MutableList<TieredCoachingAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredCoachingAuditEngine(flags: Registry): TieredCoachingAuditEngine {
    return TieredCoachingAuditEngine(
        steps = mutableListOf<TieredCoachingAuditAdjuster>(
            CapTieredCoachingAuditAdjuster(9103.0),
            FlaggedTieredCoachingAuditAdjuster(flags, "tiered-coaching-audit-regional", -0.4),
            TieredTieredCoachingAuditAdjuster(
                mutableListOf<Double>(1125.0, 1315.0),
                mutableListOf<Double>(0.87, 0.81),
            ),
            MinimumTieredCoachingAuditAdjuster(24.8),
            TieredCoachingAuditEndsAtOffsetAdjuster(1.23),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 74219.6,
    )
}
