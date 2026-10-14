package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered camera event allocation calculation. */
interface TieredCameraEventAllocationModifier {
    fun describe(): String
    fun adjust(item: TieredCameraEventAllocation, value: Double): Double
}

data class RoundingTieredCameraEventAllocationAdjuster(var step: Double) : TieredCameraEventAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredCameraEventAllocation, value: Double): Double = roundTo(value, step)
}

data class TieredCameraEventAllocationBudgetOffsetAdjuster(
    var weight: Double,
) : TieredCameraEventAllocationModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: TieredCameraEventAllocation, value: Double): Double = value + item.budget * weight
}

data class FlaggedTieredCameraEventAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCameraEventAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCameraEventAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTieredCameraEventAllocationAdjuster(
    var percent: Double,
) : TieredCameraEventAllocationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredCameraEventAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredCameraEventAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredCameraEventAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredCameraEventAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered camera event allocation steps in order and keeps a trace of each. */
data class TieredCameraEventAllocationEngine(
    var steps: MutableList<TieredCameraEventAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredCameraEventAllocation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredCameraEventAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredCameraEventAllocationEngine(flags: Registry): TieredCameraEventAllocationEngine {
    return TieredCameraEventAllocationEngine(
        steps = mutableListOf<TieredCameraEventAllocationModifier>(
            RoundingTieredCameraEventAllocationAdjuster(0.05),
            TieredCameraEventAllocationBudgetOffsetAdjuster(1.38),
            FlaggedTieredCameraEventAllocationAdjuster(flags, "tiered-camera-event-allocation-capped", -9.7),
            PercentTieredCameraEventAllocationAdjuster(-0.6),
            TieredTieredCameraEventAllocationAdjuster(
                mutableListOf<Double>(654.0, 9615.0, 9696.0),
                mutableListOf<Double>(1.15, 1.1, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 79960.2,
    )
}
