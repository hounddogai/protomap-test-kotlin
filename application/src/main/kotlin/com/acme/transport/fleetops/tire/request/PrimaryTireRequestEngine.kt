package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.flags.Registry

/** One step of the primary tire request calculation. */
interface PrimaryTireRequestFactor {
    fun describe(): String
    fun adjust(item: PrimaryTireRequest, value: Double): Double
}

data class FlaggedPrimaryTireRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTireRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTireRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryTireRequestBudgetOffsetAdjuster(var weight: Double) : PrimaryTireRequestFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: PrimaryTireRequest, value: Double): Double = value + item.budget * weight
}

data class CapPrimaryTireRequestAdjuster(var cap: Double) : PrimaryTireRequestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTireRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the primary tire request steps in order and keeps a trace of each. */
data class PrimaryTireRequestEngine(
    var steps: MutableList<PrimaryTireRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryTireRequest): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTireRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTireRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryTireRequestEngine(flags: Registry): PrimaryTireRequestEngine {
    return PrimaryTireRequestEngine(
        steps = mutableListOf<PrimaryTireRequestFactor>(
            FlaggedPrimaryTireRequestAdjuster(flags, "primary-tire-request-adaptive", 8.0),
            PrimaryTireRequestBudgetOffsetAdjuster(1.98),
            CapPrimaryTireRequestAdjuster(13956.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 25217.7,
    )
}
