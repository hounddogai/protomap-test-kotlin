package com.acme.transport.linehaul.relay.target

/** One step of the relay plan calculation. */
interface RelayPlanModifier {
    fun describe(): String
    fun adjust(item: RelayPlan, value: Double): Double
}

data class RelayPlanLimitOffsetAdjuster(var weight: Double) : RelayPlanModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: RelayPlan, value: Double): Double = value + item.limit.toDouble() * weight
}

data class TitleRelayPlanAdjuster(var factors: MutableMap<String, Double>) : RelayPlanModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RelayPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRelayPlanAdjuster(var percent: Double) : RelayPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRelayPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayPlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the relay plan steps in order and keeps a trace of each. */
data class RelayPlanEngine(
    var steps: MutableList<RelayPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayPlan): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayPlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RelayPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayPlanEngine(): RelayPlanEngine {
    return RelayPlanEngine(
        steps = mutableListOf<RelayPlanModifier>(
            RelayPlanLimitOffsetAdjuster(0.36),
            TitleRelayPlanAdjuster(
                mutableMapOf<String, Double>("online" to 1.0, "central" to 0.87, "west" to 0.8),
            ),
            PercentRelayPlanAdjuster(13.7),
            TieredRelayPlanAdjuster(mutableListOf<Double>(2242.0, 9507.0), mutableListOf<Double>(1.03, 1.0)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 6147.3,
    )
}
