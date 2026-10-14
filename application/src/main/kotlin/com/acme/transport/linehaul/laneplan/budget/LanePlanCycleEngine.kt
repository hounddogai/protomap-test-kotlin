package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lane plan cycle calculation. */
interface LanePlanCycleStep {
    fun describe(): String
    fun adjust(item: LanePlanCycle, value: Double): Double
}

data class TieredLanePlanCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelLanePlanCycleAdjuster(var factors: MutableMap<String, Double>) : LanePlanCycleStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: LanePlanCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLanePlanCycleAdjuster(var minimum: Double) : LanePlanCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLanePlanCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLanePlanCycleAdjuster(var cap: Double) : LanePlanCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanCycle, value: Double): Double = minOf(value, cap)
}

data class LanePlanCycleFuelLitersOffsetAdjuster(var weight: Double) : LanePlanCycleStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: LanePlanCycle, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the lane plan cycle steps in order and keeps a trace of each. */
data class LanePlanCycleEngine(
    var steps: MutableList<LanePlanCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LanePlanCycle): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LanePlanCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLanePlanCycleEngine(flags: Registry): LanePlanCycleEngine {
    return LanePlanCycleEngine(
        steps = mutableListOf<LanePlanCycleStep>(
            TieredLanePlanCycleAdjuster(
                mutableListOf<Double>(1478.0, 4827.0, 7731.0, 7940.0),
                mutableListOf<Double>(1.12, 0.88, 1.09, 1.04),
            ),
            ChannelLanePlanCycleAdjuster(
                mutableMapOf<String, Double>("west" to 0.76, "online" to 1.27, "north" to 0.72),
            ),
            MinimumLanePlanCycleAdjuster(34.5),
            FlaggedLanePlanCycleAdjuster(flags, "lane-plan-cycle-adaptive", 4.1),
            CapLanePlanCycleAdjuster(41997.0),
            LanePlanCycleFuelLitersOffsetAdjuster(1.66),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 28340.9,
    )
}
