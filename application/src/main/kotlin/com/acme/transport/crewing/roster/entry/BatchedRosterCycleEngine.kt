package com.acme.transport.crewing.roster.entry

/** One step of the batched roster cycle calculation. */
interface BatchedRosterCycleFactor {
    fun describe(): String
    fun adjust(item: BatchedRosterCycle, value: Double): Double
}

data class BatchedRosterCycleOdometerOffsetAdjuster(var weight: Double) : BatchedRosterCycleFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: BatchedRosterCycle, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class PercentBatchedRosterCycleAdjuster(var percent: Double) : BatchedRosterCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedRosterCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelBatchedRosterCycleAdjuster(var factors: MutableMap<String, Double>) : BatchedRosterCycleFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: BatchedRosterCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBatchedRosterCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedRosterCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedRosterCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched roster cycle steps in order and keeps a trace of each. */
data class BatchedRosterCycleEngine(
    var steps: MutableList<BatchedRosterCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedRosterCycle): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedRosterCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedRosterCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedRosterCycleEngine(): BatchedRosterCycleEngine {
    return BatchedRosterCycleEngine(
        steps = mutableListOf<BatchedRosterCycleFactor>(
            BatchedRosterCycleOdometerOffsetAdjuster(1.84),
            PercentBatchedRosterCycleAdjuster(-10.8),
            LabelBatchedRosterCycleAdjuster(mutableMapOf<String, Double>("south" to 0.85, "central" to 0.88)),
            TieredBatchedRosterCycleAdjuster(
                mutableListOf<Double>(3267.0, 4135.0),
                mutableListOf<Double>(0.8, 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 45081.1,
    )
}
