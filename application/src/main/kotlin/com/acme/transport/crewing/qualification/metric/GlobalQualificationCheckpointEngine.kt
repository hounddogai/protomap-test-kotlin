package com.acme.transport.crewing.qualification.metric

/** One step of the global qualification checkpoint calculation. */
interface GlobalQualificationCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: GlobalQualificationCheckpoint, value: Double): Double
}

data class SourceGlobalQualificationCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalQualificationCheckpointAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: GlobalQualificationCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalQualificationCheckpointAdjuster(
    var percent: Double,
) : GlobalQualificationCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalQualificationCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapGlobalQualificationCheckpointAdjuster(var cap: Double) : GlobalQualificationCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalQualificationCheckpoint, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalQualificationCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalQualificationCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalQualificationCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumGlobalQualificationCheckpointAdjuster(
    var minimum: Double,
) : GlobalQualificationCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalQualificationCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global qualification checkpoint steps in order and keeps a trace of each. */
data class GlobalQualificationCheckpointEngine(
    var steps: MutableList<GlobalQualificationCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalQualificationCheckpoint): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalQualificationCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalQualificationCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalQualificationCheckpointEngine(): GlobalQualificationCheckpointEngine {
    return GlobalQualificationCheckpointEngine(
        steps = mutableListOf<GlobalQualificationCheckpointAdjuster>(
            SourceGlobalQualificationCheckpointAdjuster(
                mutableMapOf<String, Double>("north" to 1.04, "central" to 1.17, "west" to 0.77),
            ),
            PercentGlobalQualificationCheckpointAdjuster(17.5),
            CapGlobalQualificationCheckpointAdjuster(28990.0),
            TieredGlobalQualificationCheckpointAdjuster(
                mutableListOf<Double>(885.0, 3200.0, 6690.0),
                mutableListOf<Double>(1.17, 0.92, 1.03),
            ),
            MinimumGlobalQualificationCheckpointAdjuster(44.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 3613.1,
    )
}
