package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the duty period request calculation. */
interface DutyPeriodRequestFactor {
    fun describe(): String
    fun adjust(item: DutyPeriodRequest, value: Double): Double
}

data class TieredDutyPeriodRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodRequestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDutyPeriodRequestAdjuster(var minimum: Double) : DutyPeriodRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DutyPeriodRequestLoadKgOffsetAdjuster(var weight: Double) : DutyPeriodRequestFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: DutyPeriodRequest, value: Double): Double = value + item.loadKg * weight
}

data class RegionDutyPeriodRequestAdjuster(var factors: MutableMap<String, Double>) : DutyPeriodRequestFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: DutyPeriodRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDutyPeriodRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the duty period request steps in order and keeps a trace of each. */
data class DutyPeriodRequestEngine(
    var steps: MutableList<DutyPeriodRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DutyPeriodRequest): Double {
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

    fun evaluateAll(items: MutableList<DutyPeriodRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDutyPeriodRequestEngine(flags: Registry): DutyPeriodRequestEngine {
    return DutyPeriodRequestEngine(
        steps = mutableListOf<DutyPeriodRequestFactor>(
            TieredDutyPeriodRequestAdjuster(
                mutableListOf<Double>(248.0, 306.0, 788.0, 2433.0),
                mutableListOf<Double>(0.86, 1.09, 1.04, 1.17),
            ),
            MinimumDutyPeriodRequestAdjuster(28.9),
            DutyPeriodRequestLoadKgOffsetAdjuster(0.04),
            RegionDutyPeriodRequestAdjuster(
                mutableMapOf<String, Double>("south" to 0.95, "west" to 0.84, "central" to 0.78),
            ),
            FlaggedDutyPeriodRequestAdjuster(flags, "duty-period-request-pending", 1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 80675.9,
    )
}
