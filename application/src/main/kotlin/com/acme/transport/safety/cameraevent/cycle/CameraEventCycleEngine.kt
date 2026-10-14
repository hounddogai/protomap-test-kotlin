package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the camera event cycle calculation. */
interface CameraEventCycleAdjuster {
    fun describe(): String
    fun adjust(item: CameraEventCycle, value: Double): Double
}

data class SourceCameraEventCycleAdjuster(var factors: MutableMap<String, Double>) : CameraEventCycleAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: CameraEventCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCameraEventCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCameraEventCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCameraEventCycleAdjuster(var percent: Double) : CameraEventCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCameraEventCycleAdjuster(var step: Double) : CameraEventCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the camera event cycle steps in order and keeps a trace of each. */
data class CameraEventCycleEngine(
    var steps: MutableList<CameraEventCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CameraEventCycle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCameraEventCycleEngine(flags: Registry): CameraEventCycleEngine {
    return CameraEventCycleEngine(
        steps = mutableListOf<CameraEventCycleAdjuster>(
            SourceCameraEventCycleAdjuster(
                mutableMapOf<String, Double>("north" to 1.22, "online" to 0.78, "west" to 0.75),
            ),
            TieredCameraEventCycleAdjuster(
                mutableListOf<Double>(5214.0, 8329.0),
                mutableListOf<Double>(0.94, 1.15),
            ),
            FlaggedCameraEventCycleAdjuster(flags, "camera-event-cycle-scheduled", 7.0),
            PercentCameraEventCycleAdjuster(-24.7),
            RoundingCameraEventCycleAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 64643.4,
    )
}
