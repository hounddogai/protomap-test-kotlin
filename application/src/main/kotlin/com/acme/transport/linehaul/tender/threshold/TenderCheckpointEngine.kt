package com.acme.transport.linehaul.tender.threshold

/** One step of the tender checkpoint calculation. */
interface TenderCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: TenderCheckpoint, value: Double): Double
}

data class TieredTenderCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TenderCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TenderCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTenderCheckpointAdjuster(var minimum: Double) : TenderCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleTenderCheckpointAdjuster(var factors: MutableMap<String, Double>) : TenderCheckpointAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: TenderCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tender checkpoint steps in order and keeps a trace of each. */
data class TenderCheckpointEngine(
    var steps: MutableList<TenderCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TenderCheckpoint): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TenderCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTenderCheckpointEngine(): TenderCheckpointEngine {
    return TenderCheckpointEngine(
        steps = mutableListOf<TenderCheckpointAdjuster>(
            TieredTenderCheckpointAdjuster(
                mutableListOf<Double>(1307.0, 5229.0, 7604.0, 9556.0),
                mutableListOf<Double>(1.17, 1.03, 1.14, 0.95),
            ),
            MinimumTenderCheckpointAdjuster(39.7),
            TitleTenderCheckpointAdjuster(mutableMapOf<String, Double>("east" to 1.01, "south" to 1.04)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 9581.0,
    )
}
