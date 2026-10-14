package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary emission factor group calculation. */
interface PrimaryEmissionFactorGroupModifier {
    fun describe(): String
    fun adjust(item: PrimaryEmissionFactorGroup, value: Double): Double
}

data class RoundingPrimaryEmissionFactorGroupAdjuster(var step: Double) : PrimaryEmissionFactorGroupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryEmissionFactorGroup, value: Double): Double = roundTo(value, step)
}

data class PercentPrimaryEmissionFactorGroupAdjuster(var percent: Double) : PrimaryEmissionFactorGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryEmissionFactorGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NamePrimaryEmissionFactorGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryEmissionFactorGroupModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryEmissionFactorGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPrimaryEmissionFactorGroupAdjuster(var cap: Double) : PrimaryEmissionFactorGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryEmissionFactorGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the primary emission factor group steps in order and keeps a trace of each. */
data class PrimaryEmissionFactorGroupEngine(
    var steps: MutableList<PrimaryEmissionFactorGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryEmissionFactorGroup): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryEmissionFactorGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryEmissionFactorGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryEmissionFactorGroupEngine(): PrimaryEmissionFactorGroupEngine {
    return PrimaryEmissionFactorGroupEngine(
        steps = mutableListOf<PrimaryEmissionFactorGroupModifier>(
            RoundingPrimaryEmissionFactorGroupAdjuster(0.25),
            PercentPrimaryEmissionFactorGroupAdjuster(2.8),
            NamePrimaryEmissionFactorGroupAdjuster(
                mutableMapOf<String, Double>("online" to 0.71, "central" to 0.94, "south" to 1.09),
            ),
            CapPrimaryEmissionFactorGroupAdjuster(38776.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 16498.9,
    )
}
