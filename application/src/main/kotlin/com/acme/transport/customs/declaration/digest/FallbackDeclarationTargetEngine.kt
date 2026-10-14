package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.flags.Registry

/** One step of the fallback declaration target calculation. */
interface FallbackDeclarationTargetStep {
    fun describe(): String
    fun adjust(item: FallbackDeclarationTarget, value: Double): Double
}

data class FlaggedFallbackDeclarationTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDeclarationTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDeclarationTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackDeclarationTargetAdjuster(var cap: Double) : FallbackDeclarationTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDeclarationTarget, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackDeclarationTargetAdjuster(var percent: Double) : FallbackDeclarationTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDeclarationTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback declaration target steps in order and keeps a trace of each. */
data class FallbackDeclarationTargetEngine(
    var steps: MutableList<FallbackDeclarationTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDeclarationTarget): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDeclarationTargetStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackDeclarationTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDeclarationTargetEngine(flags: Registry): FallbackDeclarationTargetEngine {
    return FallbackDeclarationTargetEngine(
        steps = mutableListOf<FallbackDeclarationTargetStep>(
            FlaggedFallbackDeclarationTargetAdjuster(flags, "fallback-declaration-target-tiered", 7.0),
            CapFallbackDeclarationTargetAdjuster(47496.0),
            PercentFallbackDeclarationTargetAdjuster(2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 12385.0,
    )
}
