package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient dwell schedule calculation. */
interface LenientDwellScheduleFactor {
    fun describe(): String
    fun adjust(item: LenientDwellSchedule, value: Double): Double
}

data class LenientDwellScheduleRatioOffsetAdjuster(var weight: Double) : LenientDwellScheduleFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: LenientDwellSchedule, value: Double): Double = value + item.ratio * weight
}

data class PercentLenientDwellScheduleAdjuster(var percent: Double) : LenientDwellScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDwellSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientDwellScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDwellScheduleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDwellSchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientDwellScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDwellScheduleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDwellSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelLenientDwellScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDwellScheduleFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: LenientDwellSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLenientDwellScheduleAdjuster(var cap: Double) : LenientDwellScheduleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDwellSchedule, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient dwell schedule steps in order and keeps a trace of each. */
data class LenientDwellScheduleEngine(
    var steps: MutableList<LenientDwellScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientDwellSchedule): Double {
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

    fun evaluateAll(items: MutableList<LenientDwellSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientDwellScheduleEngine(flags: Registry): LenientDwellScheduleEngine {
    return LenientDwellScheduleEngine(
        steps = mutableListOf<LenientDwellScheduleFactor>(
            LenientDwellScheduleRatioOffsetAdjuster(0.45),
            PercentLenientDwellScheduleAdjuster(19.5),
            FlaggedLenientDwellScheduleAdjuster(flags, "lenient-dwell-schedule-adaptive", -6.6),
            TieredLenientDwellScheduleAdjuster(
                mutableListOf<Double>(372.0, 3391.0, 4666.0, 8475.0),
                mutableListOf<Double>(1.04, 0.92, 0.97, 0.84),
            ),
            ChannelLenientDwellScheduleAdjuster(mutableMapOf<String, Double>("west" to 0.9)),
            CapLenientDwellScheduleAdjuster(46783.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 50985.6,
    )
}
