package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the camera event digest calculation. */
interface CameraEventDigestStep {
    fun describe(): String
    fun adjust(item: CameraEventDigest, value: Double): Double
}

data class MinimumCameraEventDigestAdjuster(var minimum: Double) : CameraEventDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCameraEventDigestAdjuster(var percent: Double) : CameraEventDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCameraEventDigestAdjuster(var cap: Double) : CameraEventDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventDigest, value: Double): Double = minOf(value, cap)
}

data class RegionCameraEventDigestAdjuster(var factors: MutableMap<String, Double>) : CameraEventDigestStep {
    override fun describe(): String = "by region"

    override fun adjust(item: CameraEventDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCameraEventDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CameraEventDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CameraEventDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCameraEventDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the camera event digest steps in order and keeps a trace of each. */
data class CameraEventDigestEngine(
    var steps: MutableList<CameraEventDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CameraEventDigest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CameraEventDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCameraEventDigestEngine(flags: Registry): CameraEventDigestEngine {
    return CameraEventDigestEngine(
        steps = mutableListOf<CameraEventDigestStep>(
            MinimumCameraEventDigestAdjuster(23.8),
            PercentCameraEventDigestAdjuster(-20.2),
            CapCameraEventDigestAdjuster(5307.0),
            RegionCameraEventDigestAdjuster(mutableMapOf<String, Double>("east" to 1.38)),
            FlaggedCameraEventDigestAdjuster(flags, "camera-event-digest-legacy", -9.3),
            TieredCameraEventDigestAdjuster(
                mutableListOf<Double>(3294.0, 8837.0),
                mutableListOf<Double>(1.16, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 53686.0,
    )
}
