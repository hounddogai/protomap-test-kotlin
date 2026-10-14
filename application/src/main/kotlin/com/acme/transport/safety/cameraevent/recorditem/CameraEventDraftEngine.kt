package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the camera event draft calculation. */
interface CameraEventDraftStep {
    fun describe(): String
    fun adjust(item: CameraEventDraft, value: Double): Double
}

data class RoundingCameraEventDraftAdjuster(var step: Double) : CameraEventDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventDraft, value: Double): Double = roundTo(value, step)
}

data class PercentCameraEventDraftAdjuster(var percent: Double) : CameraEventDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCameraEventDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventDraftStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CameraEventDraftScoreOffsetAdjuster(var weight: Double) : CameraEventDraftStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: CameraEventDraft, value: Double): Double = value + item.score * weight
}

/** Runs the camera event draft steps in order and keeps a trace of each. */
data class CameraEventDraftEngine(
    var steps: MutableList<CameraEventDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CameraEventDraft): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCameraEventDraftEngine(flags: Registry): CameraEventDraftEngine {
    return CameraEventDraftEngine(
        steps = mutableListOf<CameraEventDraftStep>(
            RoundingCameraEventDraftAdjuster(0.25),
            PercentCameraEventDraftAdjuster(6.5),
            FlaggedCameraEventDraftAdjuster(flags, "camera-event-draft-pending", -6.7),
            CameraEventDraftScoreOffsetAdjuster(1.16),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 38157.1,
    )
}
