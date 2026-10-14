package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the declaration plan calculation. */
interface DeclarationPlanStep {
    fun describe(): String
    fun adjust(item: DeclarationPlan, value: Double): Double
}

data class DeclarationPlanRetriesOffsetAdjuster(var weight: Double) : DeclarationPlanStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: DeclarationPlan, value: Double): Double = value + item.retries.toDouble() * weight
}

data class TieredDeclarationPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDeclarationPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the declaration plan steps in order and keeps a trace of each. */
data class DeclarationPlanEngine(
    var steps: MutableList<DeclarationPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DeclarationPlan): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDeclarationPlanEngine(flags: Registry): DeclarationPlanEngine {
    return DeclarationPlanEngine(
        steps = mutableListOf<DeclarationPlanStep>(
            DeclarationPlanRetriesOffsetAdjuster(1.75),
            TieredDeclarationPlanAdjuster(
                mutableListOf<Double>(3744.0, 4618.0, 6353.0, 8226.0),
                mutableListOf<Double>(1.08, 0.88, 0.93, 1.12),
            ),
            FlaggedDeclarationPlanAdjuster(flags, "declaration-plan-rolling", -8.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 16978.2,
    )
}
