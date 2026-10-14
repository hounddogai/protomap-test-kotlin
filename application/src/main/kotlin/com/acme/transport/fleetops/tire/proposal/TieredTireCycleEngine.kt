package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered tire cycle calculation. */
interface TieredTireCycleAdjuster {
    fun describe(): String
    fun adjust(item: TieredTireCycle, value: Double): Double
}

data class RoundingTieredTireCycleAdjuster(var step: Double) : TieredTireCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredTireCycle, value: Double): Double = roundTo(value, step)
}

data class CapTieredTireCycleAdjuster(var cap: Double) : TieredTireCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTireCycle, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredTireCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredTireCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredTireCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneTieredTireCycleAdjuster(var factors: MutableMap<String, Double>) : TieredTireCycleAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: TieredTireCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredTireCycleAdjuster(var minimum: Double) : TieredTireCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTireCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered tire cycle steps in order and keeps a trace of each. */
data class TieredTireCycleEngine(
    var steps: MutableList<TieredTireCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredTireCycle): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredTireCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredTireCycleEngine(flags: Registry): TieredTireCycleEngine {
    return TieredTireCycleEngine(
        steps = mutableListOf<TieredTireCycleAdjuster>(
            RoundingTieredTireCycleAdjuster(0.1),
            CapTieredTireCycleAdjuster(34840.0),
            FlaggedTieredTireCycleAdjuster(flags, "tiered-tire-cycle-manual", -1.3),
            LaneTieredTireCycleAdjuster(
                mutableMapOf<String, Double>("north" to 0.85, "central" to 1.17, "east" to 1.31),
            ),
            MinimumTieredTireCycleAdjuster(30.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 37674.4,
    )
}
