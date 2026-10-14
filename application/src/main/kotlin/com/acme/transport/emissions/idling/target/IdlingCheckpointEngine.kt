package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling checkpoint calculation. */
interface IdlingCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: IdlingCheckpoint, value: Double): Double
}

data class PercentIdlingCheckpointAdjuster(var percent: Double) : IdlingCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredIdlingCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumIdlingCheckpointAdjuster(var minimum: Double) : IdlingCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedIdlingCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelIdlingCheckpointAdjuster(var factors: MutableMap<String, Double>) : IdlingCheckpointAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: IdlingCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class IdlingCheckpointCreatedAtOffsetAdjuster(var weight: Double) : IdlingCheckpointAdjuster {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: IdlingCheckpoint, value: Double): Double = value + item.createdAt.toDouble() * weight
}

/** Runs the idling checkpoint steps in order and keeps a trace of each. */
data class IdlingCheckpointEngine(
    var steps: MutableList<IdlingCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IdlingCheckpoint): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIdlingCheckpointEngine(flags: Registry): IdlingCheckpointEngine {
    return IdlingCheckpointEngine(
        steps = mutableListOf<IdlingCheckpointAdjuster>(
            PercentIdlingCheckpointAdjuster(19.2),
            TieredIdlingCheckpointAdjuster(
                mutableListOf<Double>(2819.0, 4365.0, 4691.0, 5385.0),
                mutableListOf<Double>(0.96, 1.0, 1.1, 0.8),
            ),
            MinimumIdlingCheckpointAdjuster(33.8),
            FlaggedIdlingCheckpointAdjuster(flags, "idling-checkpoint-lenient", 0.3),
            LabelIdlingCheckpointAdjuster(mutableMapOf<String, Double>("south" to 1.04)),
            IdlingCheckpointCreatedAtOffsetAdjuster(1.78),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 77173.9,
    )
}
