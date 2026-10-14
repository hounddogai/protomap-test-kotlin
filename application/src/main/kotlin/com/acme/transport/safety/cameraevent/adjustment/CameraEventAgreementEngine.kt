package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the camera event agreement calculation. */
interface CameraEventAgreementAdjuster {
    fun describe(): String
    fun adjust(item: CameraEventAgreement, value: Double): Double
}

data class FlaggedCameraEventAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCameraEventAgreementAdjuster(var step: Double) : CameraEventAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CameraEventAgreement, value: Double): Double = roundTo(value, step)
}

data class CapCameraEventAgreementAdjuster(var cap: Double) : CameraEventAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventAgreement, value: Double): Double = minOf(value, cap)
}

data class CameraEventAgreementAmountOffsetAdjuster(var weight: Double) : CameraEventAgreementAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: CameraEventAgreement, value: Double): Double = value + item.amount * weight
}

/** Runs the camera event agreement steps in order and keeps a trace of each. */
data class CameraEventAgreementEngine(
    var steps: MutableList<CameraEventAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CameraEventAgreement): Double {
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

    fun evaluateAll(items: MutableList<CameraEventAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCameraEventAgreementEngine(flags: Registry): CameraEventAgreementEngine {
    return CameraEventAgreementEngine(
        steps = mutableListOf<CameraEventAgreementAdjuster>(
            FlaggedCameraEventAgreementAdjuster(flags, "camera-event-agreement-scheduled", 2.6),
            RoundingCameraEventAgreementAdjuster(0.5),
            CapCameraEventAgreementAdjuster(9250.0),
            CameraEventAgreementAmountOffsetAdjuster(0.64),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 10062.8,
    )
}
