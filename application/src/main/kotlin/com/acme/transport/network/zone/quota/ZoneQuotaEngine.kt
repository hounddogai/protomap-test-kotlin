package com.acme.transport.network.zone.quota

/** One step of the zone quota calculation. */
interface ZoneQuotaStep {
    fun describe(): String
    fun adjust(item: ZoneQuota, value: Double): Double
}

data class ZoneQuotaSequenceOffsetAdjuster(var weight: Double) : ZoneQuotaStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: ZoneQuota, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class LaneZoneQuotaAdjuster(var factors: MutableMap<String, Double>) : ZoneQuotaStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: ZoneQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumZoneQuotaAdjuster(var minimum: Double) : ZoneQuotaStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredZoneQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneQuotaStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentZoneQuotaAdjuster(var percent: Double) : ZoneQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapZoneQuotaAdjuster(var cap: Double) : ZoneQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the zone quota steps in order and keeps a trace of each. */
data class ZoneQuotaEngine(
    var steps: MutableList<ZoneQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ZoneQuota): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultZoneQuotaEngine(): ZoneQuotaEngine {
    return ZoneQuotaEngine(
        steps = mutableListOf<ZoneQuotaStep>(
            ZoneQuotaSequenceOffsetAdjuster(1.6),
            LaneZoneQuotaAdjuster(mutableMapOf<String, Double>("online" to 0.9)),
            MinimumZoneQuotaAdjuster(28.2),
            TieredZoneQuotaAdjuster(
                mutableListOf<Double>(367.0, 2451.0, 7296.0),
                mutableListOf<Double>(1.08, 0.94, 1.03),
            ),
            PercentZoneQuotaAdjuster(-2.4),
            CapZoneQuotaAdjuster(34776.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 9873.0,
    )
}
