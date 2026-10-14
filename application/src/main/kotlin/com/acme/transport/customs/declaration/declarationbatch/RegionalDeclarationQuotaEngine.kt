package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional declaration quota calculation. */
interface RegionalDeclarationQuotaModifier {
    fun describe(): String
    fun adjust(item: RegionalDeclarationQuota, value: Double): Double
}

data class CapRegionalDeclarationQuotaAdjuster(var cap: Double) : RegionalDeclarationQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDeclarationQuota, value: Double): Double = minOf(value, cap)
}

data class RoundingRegionalDeclarationQuotaAdjuster(var step: Double) : RegionalDeclarationQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDeclarationQuota, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalDeclarationQuotaAdjuster(var minimum: Double) : RegionalDeclarationQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDeclarationQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional declaration quota steps in order and keeps a trace of each. */
data class RegionalDeclarationQuotaEngine(
    var steps: MutableList<RegionalDeclarationQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDeclarationQuota): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDeclarationQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDeclarationQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDeclarationQuotaEngine(): RegionalDeclarationQuotaEngine {
    return RegionalDeclarationQuotaEngine(
        steps = mutableListOf<RegionalDeclarationQuotaModifier>(
            CapRegionalDeclarationQuotaAdjuster(45916.0),
            RoundingRegionalDeclarationQuotaAdjuster(0.05),
            MinimumRegionalDeclarationQuotaAdjuster(4.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 37141.5,
    )
}
