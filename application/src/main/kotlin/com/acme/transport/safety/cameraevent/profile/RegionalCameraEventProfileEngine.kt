package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional camera event profile calculation. */
interface RegionalCameraEventProfileFactor {
    fun describe(): String
    fun adjust(item: RegionalCameraEventProfile, value: Double): Double
}

data class TieredRegionalCameraEventProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCameraEventProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCameraEventProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleRegionalCameraEventProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCameraEventProfileFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalCameraEventProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalCameraEventProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCameraEventProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCameraEventProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalCameraEventProfileAdjuster(var percent: Double) : RegionalCameraEventProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCameraEventProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional camera event profile steps in order and keeps a trace of each. */
data class RegionalCameraEventProfileEngine(
    var steps: MutableList<RegionalCameraEventProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalCameraEventProfile): Double {
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

    fun evaluateAll(items: MutableList<RegionalCameraEventProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalCameraEventProfileEngine(flags: Registry): RegionalCameraEventProfileEngine {
    return RegionalCameraEventProfileEngine(
        steps = mutableListOf<RegionalCameraEventProfileFactor>(
            TieredRegionalCameraEventProfileAdjuster(
                mutableListOf<Double>(2822.0, 4514.0, 5481.0, 7129.0),
                mutableListOf<Double>(1.03, 1.03, 0.93, 1.02),
            ),
            TitleRegionalCameraEventProfileAdjuster(
                mutableMapOf<String, Double>("north" to 1.04, "retail" to 0.91, "central" to 1.04),
            ),
            FlaggedRegionalCameraEventProfileAdjuster(flags, "regional-camera-event-profile-lenient", -4.4),
            PercentRegionalCameraEventProfileAdjuster(7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 19643.0,
    )
}
