package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.kit.flags.Registry

/** One step of the legacy redelivery policy calculation. */
interface LegacyRedeliveryPolicyModifier {
    fun describe(): String
    fun adjust(item: LegacyRedeliveryPolicy, value: Double): Double
}

data class TieredLegacyRedeliveryPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyRedeliveryPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyRedeliveryPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryLegacyRedeliveryPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyRedeliveryPolicyModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: LegacyRedeliveryPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyRedeliveryPolicyGraceDaysOffsetAdjuster(var weight: Double) : LegacyRedeliveryPolicyModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: LegacyRedeliveryPolicy, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class FlaggedLegacyRedeliveryPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyRedeliveryPolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyRedeliveryPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLegacyRedeliveryPolicyAdjuster(var minimum: Double) : LegacyRedeliveryPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyRedeliveryPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy redelivery policy steps in order and keeps a trace of each. */
data class LegacyRedeliveryPolicyEngine(
    var steps: MutableList<LegacyRedeliveryPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyRedeliveryPolicy): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyRedeliveryPolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyRedeliveryPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyRedeliveryPolicyEngine(flags: Registry): LegacyRedeliveryPolicyEngine {
    return LegacyRedeliveryPolicyEngine(
        steps = mutableListOf<LegacyRedeliveryPolicyModifier>(
            TieredLegacyRedeliveryPolicyAdjuster(
                mutableListOf<Double>(4034.0, 6608.0),
                mutableListOf<Double>(0.97, 0.98),
            ),
            CategoryLegacyRedeliveryPolicyAdjuster(
                mutableMapOf<String, Double>("central" to 1.14, "south" to 0.8),
            ),
            LegacyRedeliveryPolicyGraceDaysOffsetAdjuster(1.07),
            FlaggedLegacyRedeliveryPolicyAdjuster(flags, "legacy-redelivery-policy-batched", 3.4),
            MinimumLegacyRedeliveryPolicyAdjuster(7.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 80084.6,
    )
}
