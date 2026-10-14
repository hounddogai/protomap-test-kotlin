package com.acme.transport.safety.cameraevent.eventslot

/** One step of the adaptive camera event rollup calculation. */
interface AdaptiveCameraEventRollupFactor {
    fun describe(): String
    fun adjust(item: AdaptiveCameraEventRollup, value: Double): Double
}

data class CapAdaptiveCameraEventRollupAdjuster(var cap: Double) : AdaptiveCameraEventRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveCameraEventRollup, value: Double): Double = minOf(value, cap)
}

data class NameAdaptiveCameraEventRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveCameraEventRollupFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveCameraEventRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AdaptiveCameraEventRollupEtaMinutesOffsetAdjuster(
    var weight: Double,
) : AdaptiveCameraEventRollupFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: AdaptiveCameraEventRollup, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class MinimumAdaptiveCameraEventRollupAdjuster(var minimum: Double) : AdaptiveCameraEventRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveCameraEventRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive camera event rollup steps in order and keeps a trace of each. */
data class AdaptiveCameraEventRollupEngine(
    var steps: MutableList<AdaptiveCameraEventRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveCameraEventRollup): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveCameraEventRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveCameraEventRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveCameraEventRollupEngine(): AdaptiveCameraEventRollupEngine {
    return AdaptiveCameraEventRollupEngine(
        steps = mutableListOf<AdaptiveCameraEventRollupFactor>(
            CapAdaptiveCameraEventRollupAdjuster(45647.0),
            NameAdaptiveCameraEventRollupAdjuster(mutableMapOf<String, Double>("south" to 1.28, "east" to 0.79)),
            AdaptiveCameraEventRollupEtaMinutesOffsetAdjuster(1.47),
            MinimumAdaptiveCameraEventRollupAdjuster(18.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 31760.9,
    )
}
