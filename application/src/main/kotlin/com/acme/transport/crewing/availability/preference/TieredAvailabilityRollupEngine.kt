package com.acme.transport.crewing.availability.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered availability rollup calculation. */
interface TieredAvailabilityRollupFactor {
    fun describe(): String
    fun adjust(item: TieredAvailabilityRollup, value: Double): Double
}

data class TieredAvailabilityRollupRetriesOffsetAdjuster(var weight: Double) : TieredAvailabilityRollupFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: TieredAvailabilityRollup, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class FlaggedTieredAvailabilityRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredAvailabilityRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredAvailabilityRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentTieredAvailabilityRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredAvailabilityRollupFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: TieredAvailabilityRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTieredAvailabilityRollupAdjuster(var percent: Double) : TieredAvailabilityRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredAvailabilityRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredAvailabilityRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredAvailabilityRollupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredAvailabilityRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered availability rollup steps in order and keeps a trace of each. */
data class TieredAvailabilityRollupEngine(
    var steps: MutableList<TieredAvailabilityRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredAvailabilityRollup): Double {
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

    fun evaluateAll(items: MutableList<TieredAvailabilityRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredAvailabilityRollupEngine(flags: Registry): TieredAvailabilityRollupEngine {
    return TieredAvailabilityRollupEngine(
        steps = mutableListOf<TieredAvailabilityRollupFactor>(
            TieredAvailabilityRollupRetriesOffsetAdjuster(1.67),
            FlaggedTieredAvailabilityRollupAdjuster(flags, "tiered-availability-rollup-default", -5.1),
            SegmentTieredAvailabilityRollupAdjuster(
                mutableMapOf<String, Double>("south" to 0.93, "retail" to 0.96, "west" to 1.39),
            ),
            PercentTieredAvailabilityRollupAdjuster(1.5),
            TieredTieredAvailabilityRollupAdjuster(
                mutableListOf<Double>(949.0, 1207.0, 6087.0, 9036.0),
                mutableListOf<Double>(0.93, 0.82, 0.88, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 40621.4,
    )
}
