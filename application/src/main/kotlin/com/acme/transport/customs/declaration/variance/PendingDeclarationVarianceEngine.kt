package com.acme.transport.customs.declaration.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending declaration variance calculation. */
interface PendingDeclarationVarianceStep {
    fun describe(): String
    fun adjust(item: PendingDeclarationVariance, value: Double): Double
}

data class FlaggedPendingDeclarationVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDeclarationVarianceStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDeclarationVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingDeclarationVarianceAdjuster(var cap: Double) : PendingDeclarationVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDeclarationVariance, value: Double): Double = minOf(value, cap)
}

data class RoundingPendingDeclarationVarianceAdjuster(var step: Double) : PendingDeclarationVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDeclarationVariance, value: Double): Double = roundTo(value, step)
}

data class PercentPendingDeclarationVarianceAdjuster(var percent: Double) : PendingDeclarationVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDeclarationVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending declaration variance steps in order and keeps a trace of each. */
data class PendingDeclarationVarianceEngine(
    var steps: MutableList<PendingDeclarationVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingDeclarationVariance): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingDeclarationVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingDeclarationVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingDeclarationVarianceEngine(flags: Registry): PendingDeclarationVarianceEngine {
    return PendingDeclarationVarianceEngine(
        steps = mutableListOf<PendingDeclarationVarianceStep>(
            FlaggedPendingDeclarationVarianceAdjuster(flags, "pending-declaration-variance-manual", -7.3),
            CapPendingDeclarationVarianceAdjuster(19113.0),
            RoundingPendingDeclarationVarianceAdjuster(0.25),
            PercentPendingDeclarationVarianceAdjuster(19.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 55306.4,
    )
}
