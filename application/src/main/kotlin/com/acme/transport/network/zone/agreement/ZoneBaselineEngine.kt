package com.acme.transport.network.zone.agreement

/** One step of the zone baseline calculation. */
interface ZoneBaselineFactor {
    fun describe(): String
    fun adjust(item: ZoneBaseline, value: Double): Double
}

data class LabelZoneBaselineAdjuster(var factors: MutableMap<String, Double>) : ZoneBaselineFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: ZoneBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ZoneBaselineGraceDaysOffsetAdjuster(var weight: Double) : ZoneBaselineFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: ZoneBaseline, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class CapZoneBaselineAdjuster(var cap: Double) : ZoneBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneBaseline, value: Double): Double = minOf(value, cap)
}

data class TieredZoneBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneBaselineFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentZoneBaselineAdjuster(var percent: Double) : ZoneBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the zone baseline steps in order and keeps a trace of each. */
data class ZoneBaselineEngine(
    var steps: MutableList<ZoneBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ZoneBaseline): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultZoneBaselineEngine(): ZoneBaselineEngine {
    return ZoneBaselineEngine(
        steps = mutableListOf<ZoneBaselineFactor>(
            LabelZoneBaselineAdjuster(mutableMapOf<String, Double>("north" to 0.93, "online" to 1.12)),
            ZoneBaselineGraceDaysOffsetAdjuster(0.11),
            CapZoneBaselineAdjuster(26781.0),
            TieredZoneBaselineAdjuster(
                mutableListOf<Double>(2484.0, 5033.0, 5386.0),
                mutableListOf<Double>(0.83, 1.14, 1.09),
            ),
            PercentZoneBaselineAdjuster(18.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 79271.6,
    )
}
