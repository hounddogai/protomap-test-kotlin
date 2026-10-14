package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.flags.Registry

/** One step of the backhaul claim calculation. */
interface BackhaulClaimModifier {
    fun describe(): String
    fun adjust(item: BackhaulClaim, value: Double): Double
}

data class CapBackhaulClaimAdjuster(var cap: Double) : BackhaulClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulClaim, value: Double): Double = minOf(value, cap)
}

data class FlaggedBackhaulClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBackhaulClaimAdjuster(var minimum: Double) : BackhaulClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameBackhaulClaimAdjuster(var factors: MutableMap<String, Double>) : BackhaulClaimModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: BackhaulClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBackhaulClaimAdjuster(var percent: Double) : BackhaulClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the backhaul claim steps in order and keeps a trace of each. */
data class BackhaulClaimEngine(
    var steps: MutableList<BackhaulClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BackhaulClaim): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BackhaulClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBackhaulClaimEngine(flags: Registry): BackhaulClaimEngine {
    return BackhaulClaimEngine(
        steps = mutableListOf<BackhaulClaimModifier>(
            CapBackhaulClaimAdjuster(25857.0),
            FlaggedBackhaulClaimAdjuster(flags, "backhaul-claim-rolling", 0.8),
            MinimumBackhaulClaimAdjuster(43.1),
            NameBackhaulClaimAdjuster(mutableMapOf<String, Double>("north" to 1.13)),
            PercentBackhaulClaimAdjuster(-13.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 13463.6,
    )
}
