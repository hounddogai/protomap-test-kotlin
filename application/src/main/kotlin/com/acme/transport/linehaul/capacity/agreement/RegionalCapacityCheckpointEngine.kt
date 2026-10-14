package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional capacity checkpoint calculation. */
interface RegionalCapacityCheckpointStep {
    fun describe(): String
    fun adjust(item: RegionalCapacityCheckpoint, value: Double): Double
}

data class RegionalCapacityCheckpointCreatedAtOffsetAdjuster(
    var weight: Double,
) : RegionalCapacityCheckpointStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: RegionalCapacityCheckpoint, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class LocaleRegionalCapacityCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCapacityCheckpointStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: RegionalCapacityCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRegionalCapacityCheckpointAdjuster(var cap: Double) : RegionalCapacityCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCapacityCheckpoint, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalCapacityCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCapacityCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCapacityCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalCapacityCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCapacityCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCapacityCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional capacity checkpoint steps in order and keeps a trace of each. */
data class RegionalCapacityCheckpointEngine(
    var steps: MutableList<RegionalCapacityCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCapacityCheckpoint): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalCapacityCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCapacityCheckpointEngine(flags: Registry): RegionalCapacityCheckpointEngine {
    return RegionalCapacityCheckpointEngine(
        steps = mutableListOf<RegionalCapacityCheckpointStep>(
            RegionalCapacityCheckpointCreatedAtOffsetAdjuster(1.82),
            LocaleRegionalCapacityCheckpointAdjuster(
                mutableMapOf<String, Double>("north" to 1.12, "retail" to 1.02, "west" to 0.98),
            ),
            CapRegionalCapacityCheckpointAdjuster(20658.0),
            FlaggedRegionalCapacityCheckpointAdjuster(flags, "regional-capacity-checkpoint-manual", 4.7),
            TieredRegionalCapacityCheckpointAdjuster(
                mutableListOf<Double>(3128.0, 3762.0, 5109.0, 7089.0),
                mutableListOf<Double>(1.18, 0.9, 1.06, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 26752.4,
    )
}
