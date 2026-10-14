package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the near miss assignment calculation. */
interface NearMissAssignmentModifier {
    fun describe(): String
    fun adjust(item: NearMissAssignment, value: Double): Double
}

data class TieredNearMissAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumNearMissAssignmentAdjuster(var minimum: Double) : NearMissAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotNearMissAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : NearMissAssignmentModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: NearMissAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the near miss assignment steps in order and keeps a trace of each. */
data class NearMissAssignmentEngine(
    var steps: MutableList<NearMissAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NearMissAssignment): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNearMissAssignmentEngine(): NearMissAssignmentEngine {
    return NearMissAssignmentEngine(
        steps = mutableListOf<NearMissAssignmentModifier>(
            TieredNearMissAssignmentAdjuster(
                mutableListOf<Double>(3941.0, 9346.0),
                mutableListOf<Double>(1.14, 0.99),
            ),
            MinimumNearMissAssignmentAdjuster(3.7),
            DepotNearMissAssignmentAdjuster(
                mutableMapOf<String, Double>("south" to 0.76, "central" to 1.15, "online" to 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 26357.6,
    )
}
