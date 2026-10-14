package com.acme.transport.fleetops.leasereturn.audit

/** One step of the lease return score calculation. */
interface LeaseReturnScoreModifier {
    fun describe(): String
    fun adjust(item: LeaseReturnScore, value: Double): Double
}

data class UnitLeaseReturnScoreAdjuster(var factors: MutableMap<String, Double>) : LeaseReturnScoreModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: LeaseReturnScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLeaseReturnScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LeaseReturnScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LeaseReturnScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LeaseReturnScoreSequenceOffsetAdjuster(var weight: Double) : LeaseReturnScoreModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: LeaseReturnScore, value: Double): Double = value + item.sequence.toDouble() * weight
}

/** Runs the lease return score steps in order and keeps a trace of each. */
data class LeaseReturnScoreEngine(
    var steps: MutableList<LeaseReturnScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LeaseReturnScore): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnScoreModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLeaseReturnScoreEngine(): LeaseReturnScoreEngine {
    return LeaseReturnScoreEngine(
        steps = mutableListOf<LeaseReturnScoreModifier>(
            UnitLeaseReturnScoreAdjuster(
                mutableMapOf<String, Double>("online" to 0.89, "retail" to 0.71, "central" to 1.24),
            ),
            TieredLeaseReturnScoreAdjuster(
                mutableListOf<Double>(423.0, 580.0, 1668.0, 6394.0),
                mutableListOf<Double>(1.08, 1.0, 0.93, 0.85),
            ),
            LeaseReturnScoreSequenceOffsetAdjuster(0.35),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 85561.3,
    )
}
