package com.acme.transport.linehaul.relay.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy relay budget calculation. */
interface LegacyRelayBudgetFactor {
    fun describe(): String
    fun adjust(item: LegacyRelayBudget, value: Double): Double
}

data class MinimumLegacyRelayBudgetAdjuster(var minimum: Double) : LegacyRelayBudgetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyRelayBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyRelayBudgetAdjuster(var percent: Double) : LegacyRelayBudgetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyRelayBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyRelayBudgetAdjuster(var step: Double) : LegacyRelayBudgetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyRelayBudget, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyRelayBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyRelayBudgetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyRelayBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameLegacyRelayBudgetAdjuster(var factors: MutableMap<String, Double>) : LegacyRelayBudgetFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyRelayBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy relay budget steps in order and keeps a trace of each. */
data class LegacyRelayBudgetEngine(
    var steps: MutableList<LegacyRelayBudgetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyRelayBudget): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyRelayBudgetFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyRelayBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyRelayBudgetEngine(flags: Registry): LegacyRelayBudgetEngine {
    return LegacyRelayBudgetEngine(
        steps = mutableListOf<LegacyRelayBudgetFactor>(
            MinimumLegacyRelayBudgetAdjuster(18.3),
            PercentLegacyRelayBudgetAdjuster(-13.6),
            RoundingLegacyRelayBudgetAdjuster(0.5),
            FlaggedLegacyRelayBudgetAdjuster(flags, "legacy-relay-budget-legacy", 1.5),
            NameLegacyRelayBudgetAdjuster(
                mutableMapOf<String, Double>("retail" to 1.29, "online" to 1.15, "central" to 1.36),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 86149.9,
    )
}
