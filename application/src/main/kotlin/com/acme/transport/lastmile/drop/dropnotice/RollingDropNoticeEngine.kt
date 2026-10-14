package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling drop notice calculation. */
interface RollingDropNoticeAdjuster {
    fun describe(): String
    fun adjust(item: RollingDropNotice, value: Double): Double
}

data class TieredRollingDropNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDropNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDropNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRollingDropNoticeAdjuster(var minimum: Double) : RollingDropNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDropNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RollingDropNoticeFloorValueOffsetAdjuster(var weight: Double) : RollingDropNoticeAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: RollingDropNotice, value: Double): Double = value + item.floorValue * weight
}

data class RoundingRollingDropNoticeAdjuster(var step: Double) : RollingDropNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDropNotice, value: Double): Double = roundTo(value, step)
}

data class CapRollingDropNoticeAdjuster(var cap: Double) : RollingDropNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDropNotice, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingDropNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDropNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDropNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling drop notice steps in order and keeps a trace of each. */
data class RollingDropNoticeEngine(
    var steps: MutableList<RollingDropNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDropNotice): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDropNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDropNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDropNoticeEngine(flags: Registry): RollingDropNoticeEngine {
    return RollingDropNoticeEngine(
        steps = mutableListOf<RollingDropNoticeAdjuster>(
            TieredRollingDropNoticeAdjuster(
                mutableListOf<Double>(1372.0, 7539.0, 8071.0),
                mutableListOf<Double>(0.99, 1.02, 1.07),
            ),
            MinimumRollingDropNoticeAdjuster(20.5),
            RollingDropNoticeFloorValueOffsetAdjuster(0.99),
            RoundingRollingDropNoticeAdjuster(0.25),
            CapRollingDropNoticeAdjuster(9712.0),
            FlaggedRollingDropNoticeAdjuster(flags, "rolling-drop-notice-lenient", -3.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 81869.9,
    )
}
