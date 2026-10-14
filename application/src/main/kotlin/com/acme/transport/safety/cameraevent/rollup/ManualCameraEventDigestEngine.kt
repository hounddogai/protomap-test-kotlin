package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual camera event digest calculation. */
interface ManualCameraEventDigestStep {
    fun describe(): String
    fun adjust(item: ManualCameraEventDigest, value: Double): Double
}

data class FlaggedManualCameraEventDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualCameraEventDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualCameraEventDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualCameraEventDigestAdjuster(var step: Double) : ManualCameraEventDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualCameraEventDigest, value: Double): Double = roundTo(value, step)
}

data class CategoryManualCameraEventDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualCameraEventDigestStep {
    override fun describe(): String = "by category"

    override fun adjust(item: ManualCameraEventDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualCameraEventDigestVersionOffsetAdjuster(var weight: Double) : ManualCameraEventDigestStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: ManualCameraEventDigest, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class MinimumManualCameraEventDigestAdjuster(var minimum: Double) : ManualCameraEventDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualCameraEventDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualCameraEventDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualCameraEventDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualCameraEventDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual camera event digest steps in order and keeps a trace of each. */
data class ManualCameraEventDigestEngine(
    var steps: MutableList<ManualCameraEventDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualCameraEventDigest): Double {
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

    fun evaluateAll(items: MutableList<ManualCameraEventDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualCameraEventDigestEngine(flags: Registry): ManualCameraEventDigestEngine {
    return ManualCameraEventDigestEngine(
        steps = mutableListOf<ManualCameraEventDigestStep>(
            FlaggedManualCameraEventDigestAdjuster(flags, "manual-camera-event-digest-default", 7.0),
            RoundingManualCameraEventDigestAdjuster(0.5),
            CategoryManualCameraEventDigestAdjuster(mutableMapOf<String, Double>("north" to 1.15)),
            ManualCameraEventDigestVersionOffsetAdjuster(1.59),
            MinimumManualCameraEventDigestAdjuster(36.2),
            TieredManualCameraEventDigestAdjuster(
                mutableListOf<Double>(493.0, 2374.0, 2650.0, 4309.0),
                mutableListOf<Double>(1.04, 1.17, 1.11, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 68980.3,
    )
}
