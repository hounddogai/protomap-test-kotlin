package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual capacity cycle calculation. */
interface ManualCapacityCycleFactor {
    fun describe(): String
    fun adjust(item: ManualCapacityCycle, value: Double): Double
}

data class MinimumManualCapacityCycleAdjuster(var minimum: Double) : ManualCapacityCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualCapacityCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedManualCapacityCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualCapacityCycleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualCapacityCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualCapacityCycleFloorValueOffsetAdjuster(var weight: Double) : ManualCapacityCycleFactor {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: ManualCapacityCycle, value: Double): Double = value + item.floorValue * weight
}

/** Runs the manual capacity cycle steps in order and keeps a trace of each. */
data class ManualCapacityCycleEngine(
    var steps: MutableList<ManualCapacityCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualCapacityCycle): Double {
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

    fun evaluateAll(items: MutableList<ManualCapacityCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualCapacityCycleEngine(flags: Registry): ManualCapacityCycleEngine {
    return ManualCapacityCycleEngine(
        steps = mutableListOf<ManualCapacityCycleFactor>(
            MinimumManualCapacityCycleAdjuster(48.0),
            FlaggedManualCapacityCycleAdjuster(flags, "manual-capacity-cycle-default", -1.6),
            ManualCapacityCycleFloorValueOffsetAdjuster(1.01),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 64277.0,
    )
}
