package com.acme.transport.freightpay.ratecard.forecast

/** One step of the fallback rate card assignment calculation. */
interface FallbackRateCardAssignmentModifier {
    fun describe(): String
    fun adjust(item: FallbackRateCardAssignment, value: Double): Double
}

data class NameFallbackRateCardAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRateCardAssignmentModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackRateCardAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFallbackRateCardAssignmentAdjuster(var minimum: Double) : FallbackRateCardAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRateCardAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackRateCardAssignmentBatchSizeOffsetAdjuster(
    var weight: Double,
) : FallbackRateCardAssignmentModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: FallbackRateCardAssignment, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

/** Runs the fallback rate card assignment steps in order and keeps a trace of each. */
data class FallbackRateCardAssignmentEngine(
    var steps: MutableList<FallbackRateCardAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackRateCardAssignment): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRateCardAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRateCardAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackRateCardAssignmentEngine(): FallbackRateCardAssignmentEngine {
    return FallbackRateCardAssignmentEngine(
        steps = mutableListOf<FallbackRateCardAssignmentModifier>(
            NameFallbackRateCardAssignmentAdjuster(mutableMapOf<String, Double>("central" to 0.94)),
            MinimumFallbackRateCardAssignmentAdjuster(48.4),
            FallbackRateCardAssignmentBatchSizeOffsetAdjuster(0.44),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 95731.0,
    )
}
