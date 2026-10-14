package com.acme.transport.safety.incident.claim

/** One step of the incident score calculation. */
interface IncidentScoreModifier {
    fun describe(): String
    fun adjust(item: IncidentScore, value: Double): Double
}

data class TieredIncidentScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionIncidentScoreAdjuster(var factors: MutableMap<String, Double>) : IncidentScoreModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: IncidentScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapIncidentScoreAdjuster(var cap: Double) : IncidentScoreModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentScore, value: Double): Double = minOf(value, cap)
}

data class PercentIncidentScoreAdjuster(var percent: Double) : IncidentScoreModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IncidentScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class IncidentScoreThresholdOffsetAdjuster(var weight: Double) : IncidentScoreModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: IncidentScore, value: Double): Double = value + item.threshold * weight
}

/** Runs the incident score steps in order and keeps a trace of each. */
data class IncidentScoreEngine(
    var steps: MutableList<IncidentScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IncidentScore): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentScoreModifier = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIncidentScoreEngine(): IncidentScoreEngine {
    return IncidentScoreEngine(
        steps = mutableListOf<IncidentScoreModifier>(
            TieredIncidentScoreAdjuster(
                mutableListOf<Double>(1931.0, 4751.0, 8622.0),
                mutableListOf<Double>(0.95, 0.98, 1.03),
            ),
            RegionIncidentScoreAdjuster(
                mutableMapOf<String, Double>("retail" to 0.99, "north" to 0.86, "south" to 1.3),
            ),
            CapIncidentScoreAdjuster(34430.0),
            PercentIncidentScoreAdjuster(-6.4),
            IncidentScoreThresholdOffsetAdjuster(0.32),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 38655.1,
    )
}
