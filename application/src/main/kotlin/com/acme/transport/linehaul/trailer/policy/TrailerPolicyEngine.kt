package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.flags.Registry

/** One step of the trailer policy calculation. */
interface TrailerPolicyModifier {
    fun describe(): String
    fun adjust(item: TrailerPolicy, value: Double): Double
}

data class PercentTrailerPolicyAdjuster(var percent: Double) : TrailerPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryTrailerPolicyAdjuster(var factors: MutableMap<String, Double>) : TrailerPolicyModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: TrailerPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTrailerPolicyAdjuster(var minimum: Double) : TrailerPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTrailerPolicyAdjuster(var cap: Double) : TrailerPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedTrailerPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerPolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the trailer policy steps in order and keeps a trace of each. */
data class TrailerPolicyEngine(
    var steps: MutableList<TrailerPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerPolicy): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerPolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerPolicyEngine(flags: Registry): TrailerPolicyEngine {
    return TrailerPolicyEngine(
        steps = mutableListOf<TrailerPolicyModifier>(
            PercentTrailerPolicyAdjuster(-15.2),
            CategoryTrailerPolicyAdjuster(mutableMapOf<String, Double>("central" to 1.01)),
            MinimumTrailerPolicyAdjuster(11.7),
            CapTrailerPolicyAdjuster(5427.0),
            FlaggedTrailerPolicyAdjuster(flags, "trailer-policy-primary", 2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 64487.5,
    )
}
