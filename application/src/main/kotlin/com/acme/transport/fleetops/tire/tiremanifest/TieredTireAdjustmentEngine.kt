package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered tire adjustment calculation. */
interface TieredTireAdjustmentStep {
    fun describe(): String
    fun adjust(item: TieredTireAdjustment, value: Double): Double
}

data class LabelTieredTireAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredTireAdjustmentStep {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredTireAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTieredTireAdjustmentAdjuster(var cap: Double) : TieredTireAdjustmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTireAdjustment, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredTireAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredTireAdjustmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredTireAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTieredTireAdjustmentAdjuster(var minimum: Double) : TieredTireAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTireAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTieredTireAdjustmentAdjuster(var percent: Double) : TieredTireAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredTireAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTireAdjustmentCreatedAtOffsetAdjuster(var weight: Double) : TieredTireAdjustmentStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: TieredTireAdjustment, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

/** Runs the tiered tire adjustment steps in order and keeps a trace of each. */
data class TieredTireAdjustmentEngine(
    var steps: MutableList<TieredTireAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredTireAdjustment): Double {
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

    fun evaluateAll(items: MutableList<TieredTireAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredTireAdjustmentEngine(flags: Registry): TieredTireAdjustmentEngine {
    return TieredTireAdjustmentEngine(
        steps = mutableListOf<TieredTireAdjustmentStep>(
            LabelTieredTireAdjustmentAdjuster(mutableMapOf<String, Double>("east" to 0.86, "south" to 0.9)),
            CapTieredTireAdjustmentAdjuster(8429.0),
            FlaggedTieredTireAdjustmentAdjuster(flags, "tiered-tire-adjustment-tiered", -9.9),
            MinimumTieredTireAdjustmentAdjuster(32.3),
            PercentTieredTireAdjustmentAdjuster(-22.2),
            TieredTireAdjustmentCreatedAtOffsetAdjuster(1.57),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 92498.4,
    )
}
