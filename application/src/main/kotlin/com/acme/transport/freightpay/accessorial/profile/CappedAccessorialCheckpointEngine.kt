package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped accessorial checkpoint calculation. */
interface CappedAccessorialCheckpointFactor {
    fun describe(): String
    fun adjust(item: CappedAccessorialCheckpoint, value: Double): Double
}

data class NameCappedAccessorialCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedAccessorialCheckpointFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedAccessorialCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedAccessorialCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedAccessorialCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedAccessorialCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedAccessorialCheckpointAdjuster(var percent: Double) : CappedAccessorialCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedAccessorialCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CappedAccessorialCheckpointPriorityOffsetAdjuster(
    var weight: Double,
) : CappedAccessorialCheckpointFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: CappedAccessorialCheckpoint, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class MinimumCappedAccessorialCheckpointAdjuster(var minimum: Double) : CappedAccessorialCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedAccessorialCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedAccessorialCheckpointAdjuster(var cap: Double) : CappedAccessorialCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedAccessorialCheckpoint, value: Double): Double = minOf(value, cap)
}

/** Runs the capped accessorial checkpoint steps in order and keeps a trace of each. */
data class CappedAccessorialCheckpointEngine(
    var steps: MutableList<CappedAccessorialCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedAccessorialCheckpoint): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedAccessorialCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedAccessorialCheckpointEngine(): CappedAccessorialCheckpointEngine {
    return CappedAccessorialCheckpointEngine(
        steps = mutableListOf<CappedAccessorialCheckpointFactor>(
            NameCappedAccessorialCheckpointAdjuster(mutableMapOf<String, Double>("west" to 1.32)),
            TieredCappedAccessorialCheckpointAdjuster(
                mutableListOf<Double>(7982.0, 8118.0, 8230.0),
                mutableListOf<Double>(1.01, 1.09, 1.03),
            ),
            PercentCappedAccessorialCheckpointAdjuster(-18.1),
            CappedAccessorialCheckpointPriorityOffsetAdjuster(0.35),
            MinimumCappedAccessorialCheckpointAdjuster(15.9),
            CapCappedAccessorialCheckpointAdjuster(49981.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 58332.7,
    )
}
