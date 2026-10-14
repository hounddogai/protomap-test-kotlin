package com.acme.transport.fleetops.defect.notice

/** One step of the fallback defect record calculation. */
interface FallbackDefectRecordModifier {
    fun describe(): String
    fun adjust(item: FallbackDefectRecord, value: Double): Double
}

data class MinimumFallbackDefectRecordAdjuster(var minimum: Double) : FallbackDefectRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDefectRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackDefectRecordScoreOffsetAdjuster(var weight: Double) : FallbackDefectRecordModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: FallbackDefectRecord, value: Double): Double = value + item.score * weight
}

data class UnitFallbackDefectRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackDefectRecordModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: FallbackDefectRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback defect record steps in order and keeps a trace of each. */
data class FallbackDefectRecordEngine(
    var steps: MutableList<FallbackDefectRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDefectRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDefectRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackDefectRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDefectRecordEngine(): FallbackDefectRecordEngine {
    return FallbackDefectRecordEngine(
        steps = mutableListOf<FallbackDefectRecordModifier>(
            MinimumFallbackDefectRecordAdjuster(13.8),
            FallbackDefectRecordScoreOffsetAdjuster(1.14),
            UnitFallbackDefectRecordAdjuster(mutableMapOf<String, Double>("west" to 1.22, "north" to 0.75)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 16946.4,
    )
}
