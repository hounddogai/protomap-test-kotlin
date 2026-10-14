package com.acme.transport.safety.cameraevent.eventbatch

import com.acme.transport.kit.flags.Registry

/** One step of the camera event segment calculation. */
interface CameraEventSegmentModifier {
    fun describe(): String
    fun adjust(item: CameraEventSegment, value: Double): Double
}

data class CameraEventSegmentCeilingOffsetAdjuster(var weight: Double) : CameraEventSegmentModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: CameraEventSegment, value: Double): Double = value + item.ceiling * weight
}

data class FlaggedCameraEventSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCameraEventSegmentAdjuster(var cap: Double) : CameraEventSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventSegment, value: Double): Double = minOf(value, cap)
}

data class TitleCameraEventSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : CameraEventSegmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CameraEventSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the camera event segment steps in order and keeps a trace of each. */
data class CameraEventSegmentEngine(
    var steps: MutableList<CameraEventSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CameraEventSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventSegmentModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCameraEventSegmentEngine(flags: Registry): CameraEventSegmentEngine {
    return CameraEventSegmentEngine(
        steps = mutableListOf<CameraEventSegmentModifier>(
            CameraEventSegmentCeilingOffsetAdjuster(1.07),
            FlaggedCameraEventSegmentAdjuster(flags, "camera-event-segment-tiered", 7.5),
            CapCameraEventSegmentAdjuster(27411.0),
            TitleCameraEventSegmentAdjuster(mutableMapOf<String, Double>("west" to 0.78, "east" to 0.84)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 23337.2,
    )
}
