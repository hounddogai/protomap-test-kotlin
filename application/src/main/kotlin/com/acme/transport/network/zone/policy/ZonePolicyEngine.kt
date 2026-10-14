package com.acme.transport.network.zone.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the zone policy calculation. */
interface ZonePolicyStep {
    fun describe(): String
    fun adjust(item: ZonePolicy, value: Double): Double
}

data class TieredZonePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZonePolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZonePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotZonePolicyAdjuster(var factors: MutableMap<String, Double>) : ZonePolicyStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: ZonePolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ZonePolicyGraceDaysOffsetAdjuster(var weight: Double) : ZonePolicyStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: ZonePolicy, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class RoundingZonePolicyAdjuster(var step: Double) : ZonePolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ZonePolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the zone policy steps in order and keeps a trace of each. */
data class ZonePolicyEngine(
    var steps: MutableList<ZonePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ZonePolicy): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZonePolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<ZonePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultZonePolicyEngine(): ZonePolicyEngine {
    return ZonePolicyEngine(
        steps = mutableListOf<ZonePolicyStep>(
            TieredZonePolicyAdjuster(mutableListOf<Double>(176.0, 4264.0), mutableListOf<Double>(0.99, 1.04)),
            DepotZonePolicyAdjuster(
                mutableMapOf<String, Double>("south" to 1.23, "retail" to 0.97, "online" to 1.37),
            ),
            ZonePolicyGraceDaysOffsetAdjuster(0.18),
            RoundingZonePolicyAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 38095.0,
    )
}
