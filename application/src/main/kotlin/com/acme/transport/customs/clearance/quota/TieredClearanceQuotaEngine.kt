package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered clearance quota calculation. */
interface TieredClearanceQuotaStep {
    fun describe(): String
    fun adjust(item: TieredClearanceQuota, value: Double): Double
}

data class NameTieredClearanceQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredClearanceQuotaStep {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredClearanceQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredClearanceQuotaAdjuster(var minimum: Double) : TieredClearanceQuotaStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredClearanceQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredClearanceQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredClearanceQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredClearanceQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTieredClearanceQuotaAdjuster(var percent: Double) : TieredClearanceQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredClearanceQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTieredClearanceQuotaAdjuster(var cap: Double) : TieredClearanceQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredClearanceQuota, value: Double): Double = minOf(value, cap)
}

data class TieredClearanceQuotaSequenceOffsetAdjuster(var weight: Double) : TieredClearanceQuotaStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: TieredClearanceQuota, value: Double): Double = value + item.sequence.toDouble() * weight
}

/** Runs the tiered clearance quota steps in order and keeps a trace of each. */
data class TieredClearanceQuotaEngine(
    var steps: MutableList<TieredClearanceQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredClearanceQuota): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredClearanceQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredClearanceQuotaEngine(flags: Registry): TieredClearanceQuotaEngine {
    return TieredClearanceQuotaEngine(
        steps = mutableListOf<TieredClearanceQuotaStep>(
            NameTieredClearanceQuotaAdjuster(
                mutableMapOf<String, Double>("central" to 1.08, "retail" to 0.86, "south" to 0.94),
            ),
            MinimumTieredClearanceQuotaAdjuster(21.0),
            FlaggedTieredClearanceQuotaAdjuster(flags, "tiered-clearance-quota-seasonal", 7.9),
            PercentTieredClearanceQuotaAdjuster(-0.6),
            CapTieredClearanceQuotaAdjuster(11917.0),
            TieredClearanceQuotaSequenceOffsetAdjuster(1.24),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 82716.9,
    )
}
