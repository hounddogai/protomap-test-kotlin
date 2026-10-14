package com.acme.transport.safety.incident.batch

/** One step of the global incident batch calculation. */
interface GlobalIncidentBatchModifier {
    fun describe(): String
    fun adjust(item: GlobalIncidentBatch, value: Double): Double
}

data class TitleGlobalIncidentBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalIncidentBatchModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalIncidentBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalIncidentBatchAdjuster(var percent: Double) : GlobalIncidentBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalIncidentBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredGlobalIncidentBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalIncidentBatchModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalIncidentBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global incident batch steps in order and keeps a trace of each. */
data class GlobalIncidentBatchEngine(
    var steps: MutableList<GlobalIncidentBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalIncidentBatch): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalIncidentBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalIncidentBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalIncidentBatchEngine(): GlobalIncidentBatchEngine {
    return GlobalIncidentBatchEngine(
        steps = mutableListOf<GlobalIncidentBatchModifier>(
            TitleGlobalIncidentBatchAdjuster(
                mutableMapOf<String, Double>("retail" to 1.29, "west" to 0.81, "online" to 1.16),
            ),
            PercentGlobalIncidentBatchAdjuster(13.3),
            TieredGlobalIncidentBatchAdjuster(
                mutableListOf<Double>(1008.0, 2533.0, 7888.0),
                mutableListOf<Double>(0.87, 1.05, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 65774.8,
    )
}
