package com.acme.transport.crewing.qualification.template

/** One step of the batched qualification cycle calculation. */
interface BatchedQualificationCycleModifier {
    fun describe(): String
    fun adjust(item: BatchedQualificationCycle, value: Double): Double
}

data class BatchedQualificationCycleWeightOffsetAdjuster(
    var weight: Double,
) : BatchedQualificationCycleModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: BatchedQualificationCycle, value: Double): Double = value + item.weight * weight
}

data class PercentBatchedQualificationCycleAdjuster(var percent: Double) : BatchedQualificationCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedQualificationCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBatchedQualificationCycleAdjuster(var cap: Double) : BatchedQualificationCycleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedQualificationCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the batched qualification cycle steps in order and keeps a trace of each. */
data class BatchedQualificationCycleEngine(
    var steps: MutableList<BatchedQualificationCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedQualificationCycle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedQualificationCycleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedQualificationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedQualificationCycleEngine(): BatchedQualificationCycleEngine {
    return BatchedQualificationCycleEngine(
        steps = mutableListOf<BatchedQualificationCycleModifier>(
            BatchedQualificationCycleWeightOffsetAdjuster(1.37),
            PercentBatchedQualificationCycleAdjuster(-2.0),
            CapBatchedQualificationCycleAdjuster(27242.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 17654.1,
    )
}
