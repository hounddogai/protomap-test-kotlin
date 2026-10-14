package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tire adjustment calculation. */
interface TireAdjustmentModifier {
    fun describe(): String
    fun adjust(item: TireAdjustment, value: Double): Double
}

data class TireAdjustmentLoadKgOffsetAdjuster(var weight: Double) : TireAdjustmentModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: TireAdjustment, value: Double): Double = value + item.loadKg * weight
}

data class LabelTireAdjustmentAdjuster(var factors: MutableMap<String, Double>) : TireAdjustmentModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TireAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTireAdjustmentAdjuster(var step: Double) : TireAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredTireAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTireAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTireAdjustmentAdjuster(var cap: Double) : TireAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireAdjustment, value: Double): Double = minOf(value, cap)
}

/** Runs the tire adjustment steps in order and keeps a trace of each. */
data class TireAdjustmentEngine(
    var steps: MutableList<TireAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireAdjustment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireAdjustmentEngine(flags: Registry): TireAdjustmentEngine {
    return TireAdjustmentEngine(
        steps = mutableListOf<TireAdjustmentModifier>(
            TireAdjustmentLoadKgOffsetAdjuster(1.0),
            LabelTireAdjustmentAdjuster(
                mutableMapOf<String, Double>("retail" to 1.35, "west" to 0.84, "north" to 0.84),
            ),
            RoundingTireAdjustmentAdjuster(0.1),
            TieredTireAdjustmentAdjuster(
                mutableListOf<Double>(173.0, 3055.0, 7721.0, 9281.0),
                mutableListOf<Double>(1.16, 1.0, 1.05, 0.91),
            ),
            FlaggedTireAdjustmentAdjuster(flags, "tire-adjustment-legacy", -2.9),
            CapTireAdjustmentAdjuster(38811.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 14059.5,
    )
}
