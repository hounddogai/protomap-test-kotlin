package com.acme.transport.lastmile.attempt.request

import com.acme.transport.kit.flags.Registry

/** One step of the attempt request calculation. */
interface AttemptRequestAdjuster {
    fun describe(): String
    fun adjust(item: AttemptRequest, value: Double): Double
}

data class TieredAttemptRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptRequestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AttemptRequestFloorValueOffsetAdjuster(var weight: Double) : AttemptRequestAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: AttemptRequest, value: Double): Double = value + item.floorValue * weight
}

data class PercentAttemptRequestAdjuster(var percent: Double) : AttemptRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAttemptRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptRequestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAttemptRequestAdjuster(var cap: Double) : AttemptRequestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the attempt request steps in order and keeps a trace of each. */
data class AttemptRequestEngine(
    var steps: MutableList<AttemptRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptRequest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptRequestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptRequestEngine(flags: Registry): AttemptRequestEngine {
    return AttemptRequestEngine(
        steps = mutableListOf<AttemptRequestAdjuster>(
            TieredAttemptRequestAdjuster(
                mutableListOf<Double>(5832.0, 7608.0, 7945.0, 9291.0),
                mutableListOf<Double>(1.2, 0.92, 1.0, 0.88),
            ),
            AttemptRequestFloorValueOffsetAdjuster(0.45),
            PercentAttemptRequestAdjuster(15.4),
            FlaggedAttemptRequestAdjuster(flags, "attempt-request-global", 6.1),
            CapAttemptRequestAdjuster(4155.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 18612.1,
    )
}
