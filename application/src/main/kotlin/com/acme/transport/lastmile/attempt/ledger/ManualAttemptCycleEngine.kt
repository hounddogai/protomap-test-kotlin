package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual attempt cycle calculation. */
interface ManualAttemptCycleModifier {
    fun describe(): String
    fun adjust(item: ManualAttemptCycle, value: Double): Double
}

data class MinimumManualAttemptCycleAdjuster(var minimum: Double) : ManualAttemptCycleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAttemptCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameManualAttemptCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualAttemptCycleModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualAttemptCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedManualAttemptCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualAttemptCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualAttemptCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual attempt cycle steps in order and keeps a trace of each. */
data class ManualAttemptCycleEngine(
    var steps: MutableList<ManualAttemptCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualAttemptCycle): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualAttemptCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualAttemptCycleEngine(flags: Registry): ManualAttemptCycleEngine {
    return ManualAttemptCycleEngine(
        steps = mutableListOf<ManualAttemptCycleModifier>(
            MinimumManualAttemptCycleAdjuster(3.0),
            NameManualAttemptCycleAdjuster(mutableMapOf<String, Double>("north" to 1.04)),
            FlaggedManualAttemptCycleAdjuster(flags, "manual-attempt-cycle-rolling", 5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 24545.0,
    )
}
