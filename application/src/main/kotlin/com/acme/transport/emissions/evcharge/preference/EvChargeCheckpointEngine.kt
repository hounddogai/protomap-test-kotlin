package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the ev charge checkpoint calculation. */
interface EvChargeCheckpointFactor {
    fun describe(): String
    fun adjust(item: EvChargeCheckpoint, value: Double): Double
}

data class CapEvChargeCheckpointAdjuster(var cap: Double) : EvChargeCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeCheckpoint, value: Double): Double = minOf(value, cap)
}

data class PercentEvChargeCheckpointAdjuster(var percent: Double) : EvChargeCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedEvChargeCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredEvChargeCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumEvChargeCheckpointAdjuster(var minimum: Double) : EvChargeCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the ev charge checkpoint steps in order and keeps a trace of each. */
data class EvChargeCheckpointEngine(
    var steps: MutableList<EvChargeCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EvChargeCheckpoint): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEvChargeCheckpointEngine(flags: Registry): EvChargeCheckpointEngine {
    return EvChargeCheckpointEngine(
        steps = mutableListOf<EvChargeCheckpointFactor>(
            CapEvChargeCheckpointAdjuster(49625.0),
            PercentEvChargeCheckpointAdjuster(-8.2),
            FlaggedEvChargeCheckpointAdjuster(flags, "ev-charge-checkpoint-lenient", -5.5),
            TieredEvChargeCheckpointAdjuster(
                mutableListOf<Double>(4084.0, 5654.0, 7899.0),
                mutableListOf<Double>(0.97, 1.01, 1.07),
            ),
            MinimumEvChargeCheckpointAdjuster(43.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 47058.8,
    )
}
