package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the camera event rollup calculation. */
interface CameraEventRollupStep {
    fun describe(): String
    fun adjust(item: CameraEventRollup, value: Double): Double
}

data class CapCameraEventRollupAdjuster(var cap: Double) : CameraEventRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventRollup, value: Double): Double = minOf(value, cap)
}

data class FlaggedCameraEventRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCameraEventRollupAdjuster(var minimum: Double) : CameraEventRollupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelCameraEventRollupAdjuster(var factors: MutableMap<String, Double>) : CameraEventRollupStep {
    override fun describe(): String = "by label"

    override fun adjust(item: CameraEventRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the camera event rollup steps in order and keeps a trace of each. */
data class CameraEventRollupEngine(
    var steps: MutableList<CameraEventRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CameraEventRollup): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCameraEventRollupEngine(flags: Registry): CameraEventRollupEngine {
    return CameraEventRollupEngine(
        steps = mutableListOf<CameraEventRollupStep>(
            CapCameraEventRollupAdjuster(1248.0),
            FlaggedCameraEventRollupAdjuster(flags, "camera-event-rollup-capped", 3.4),
            MinimumCameraEventRollupAdjuster(40.1),
            LabelCameraEventRollupAdjuster(mutableMapOf<String, Double>("east" to 0.85, "retail" to 1.31)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 98273.6,
    )
}
