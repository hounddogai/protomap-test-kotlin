package com.acme.transport.emissions.emissionfactor.factorcycle

/** One step of the batched emission factor segment calculation. */
interface BatchedEmissionFactorSegmentFactor {
    fun describe(): String
    fun adjust(item: BatchedEmissionFactorSegment, value: Double): Double
}

data class BatchedEmissionFactorSegmentWindowDaysOffsetAdjuster(
    var weight: Double,
) : BatchedEmissionFactorSegmentFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: BatchedEmissionFactorSegment, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class PercentBatchedEmissionFactorSegmentAdjuster(
    var percent: Double,
) : BatchedEmissionFactorSegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedEmissionFactorSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitBatchedEmissionFactorSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedEmissionFactorSegmentFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: BatchedEmissionFactorSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched emission factor segment steps in order and keeps a trace of each. */
data class BatchedEmissionFactorSegmentEngine(
    var steps: MutableList<BatchedEmissionFactorSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedEmissionFactorSegment): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedEmissionFactorSegmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedEmissionFactorSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedEmissionFactorSegmentEngine(): BatchedEmissionFactorSegmentEngine {
    return BatchedEmissionFactorSegmentEngine(
        steps = mutableListOf<BatchedEmissionFactorSegmentFactor>(
            BatchedEmissionFactorSegmentWindowDaysOffsetAdjuster(0.51),
            PercentBatchedEmissionFactorSegmentAdjuster(14.3),
            UnitBatchedEmissionFactorSegmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.37, "retail" to 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 90306.2,
    )
}
