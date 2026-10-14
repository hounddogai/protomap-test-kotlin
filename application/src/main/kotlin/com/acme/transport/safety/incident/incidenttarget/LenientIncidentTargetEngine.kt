package com.acme.transport.safety.incident.incidenttarget

/** One step of the lenient incident target calculation. */
interface LenientIncidentTargetModifier {
    fun describe(): String
    fun adjust(item: LenientIncidentTarget, value: Double): Double
}

data class LenientIncidentTargetBatchSizeOffsetAdjuster(var weight: Double) : LenientIncidentTargetModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: LenientIncidentTarget, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class LaneLenientIncidentTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientIncidentTargetModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: LenientIncidentTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientIncidentTargetAdjuster(var minimum: Double) : LenientIncidentTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientIncidentTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient incident target steps in order and keeps a trace of each. */
data class LenientIncidentTargetEngine(
    var steps: MutableList<LenientIncidentTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientIncidentTarget): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientIncidentTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientIncidentTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientIncidentTargetEngine(): LenientIncidentTargetEngine {
    return LenientIncidentTargetEngine(
        steps = mutableListOf<LenientIncidentTargetModifier>(
            LenientIncidentTargetBatchSizeOffsetAdjuster(0.04),
            LaneLenientIncidentTargetAdjuster(
                mutableMapOf<String, Double>("retail" to 0.82, "central" to 1.1, "west" to 1.03),
            ),
            MinimumLenientIncidentTargetAdjuster(49.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 10927.3,
    )
}
