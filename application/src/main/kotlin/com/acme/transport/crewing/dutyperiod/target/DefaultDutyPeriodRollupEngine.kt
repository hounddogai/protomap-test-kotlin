package com.acme.transport.crewing.dutyperiod.target

/** One step of the default duty period rollup calculation. */
interface DefaultDutyPeriodRollupFactor {
    fun describe(): String
    fun adjust(item: DefaultDutyPeriodRollup, value: Double): Double
}

data class CapDefaultDutyPeriodRollupAdjuster(var cap: Double) : DefaultDutyPeriodRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDutyPeriodRollup, value: Double): Double = minOf(value, cap)
}

data class DefaultDutyPeriodRollupAmountOffsetAdjuster(var weight: Double) : DefaultDutyPeriodRollupFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: DefaultDutyPeriodRollup, value: Double): Double = value + item.amount * weight
}

data class MinimumDefaultDutyPeriodRollupAdjuster(var minimum: Double) : DefaultDutyPeriodRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultDutyPeriodRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default duty period rollup steps in order and keeps a trace of each. */
data class DefaultDutyPeriodRollupEngine(
    var steps: MutableList<DefaultDutyPeriodRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultDutyPeriodRollup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDutyPeriodRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDutyPeriodRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultDutyPeriodRollupEngine(): DefaultDutyPeriodRollupEngine {
    return DefaultDutyPeriodRollupEngine(
        steps = mutableListOf<DefaultDutyPeriodRollupFactor>(
            CapDefaultDutyPeriodRollupAdjuster(4063.0),
            DefaultDutyPeriodRollupAmountOffsetAdjuster(0.96),
            MinimumDefaultDutyPeriodRollupAdjuster(46.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 18397.8,
    )
}
