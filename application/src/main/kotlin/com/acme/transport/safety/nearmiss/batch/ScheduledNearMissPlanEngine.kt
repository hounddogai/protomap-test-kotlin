package com.acme.transport.safety.nearmiss.batch

/** One step of the scheduled near miss plan calculation. */
interface ScheduledNearMissPlanStep {
    fun describe(): String
    fun adjust(item: ScheduledNearMissPlan, value: Double): Double
}

data class TieredScheduledNearMissPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledNearMissPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledNearMissPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledNearMissPlanAdjuster(var cap: Double) : ScheduledNearMissPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledNearMissPlan, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledNearMissPlanAdjuster(var percent: Double) : ScheduledNearMissPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledNearMissPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumScheduledNearMissPlanAdjuster(var minimum: Double) : ScheduledNearMissPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledNearMissPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelScheduledNearMissPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledNearMissPlanStep {
    override fun describe(): String = "by label"

    override fun adjust(item: ScheduledNearMissPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ScheduledNearMissPlanMarginOffsetAdjuster(var weight: Double) : ScheduledNearMissPlanStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ScheduledNearMissPlan, value: Double): Double = value + item.margin * weight
}

/** Runs the scheduled near miss plan steps in order and keeps a trace of each. */
data class ScheduledNearMissPlanEngine(
    var steps: MutableList<ScheduledNearMissPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledNearMissPlan): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledNearMissPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledNearMissPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledNearMissPlanEngine(): ScheduledNearMissPlanEngine {
    return ScheduledNearMissPlanEngine(
        steps = mutableListOf<ScheduledNearMissPlanStep>(
            TieredScheduledNearMissPlanAdjuster(
                mutableListOf<Double>(4075.0, 6227.0),
                mutableListOf<Double>(1.03, 0.93),
            ),
            CapScheduledNearMissPlanAdjuster(3515.0),
            PercentScheduledNearMissPlanAdjuster(11.9),
            MinimumScheduledNearMissPlanAdjuster(23.1),
            LabelScheduledNearMissPlanAdjuster(mutableMapOf<String, Double>("retail" to 1.39)),
            ScheduledNearMissPlanMarginOffsetAdjuster(1.09),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 88767.4,
    )
}
