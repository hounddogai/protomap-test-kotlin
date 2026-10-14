package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.flags.Registry

/** One step of the idling group calculation. */
interface IdlingGroupFactor {
    fun describe(): String
    fun adjust(item: IdlingGroup, value: Double): Double
}

data class PercentIdlingGroupAdjuster(var percent: Double) : IdlingGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedIdlingGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredIdlingGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the idling group steps in order and keeps a trace of each. */
data class IdlingGroupEngine(
    var steps: MutableList<IdlingGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IdlingGroup): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingGroupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIdlingGroupEngine(flags: Registry): IdlingGroupEngine {
    return IdlingGroupEngine(
        steps = mutableListOf<IdlingGroupFactor>(
            PercentIdlingGroupAdjuster(-10.1),
            FlaggedIdlingGroupAdjuster(flags, "idling-group-scheduled", 9.9),
            TieredIdlingGroupAdjuster(
                mutableListOf<Double>(469.0, 1713.0, 5719.0),
                mutableListOf<Double>(0.96, 0.87, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 49904.5,
    )
}
