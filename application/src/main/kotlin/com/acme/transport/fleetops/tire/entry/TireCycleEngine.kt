package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tire cycle calculation. */
interface TireCycleAdjuster {
    fun describe(): String
    fun adjust(item: TireCycle, value: Double): Double
}

data class FlaggedTireCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TireCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TireCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTireCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTireCycleAdjuster(var percent: Double) : TireCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTireCycleAdjuster(var cap: Double) : TireCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireCycle, value: Double): Double = minOf(value, cap)
}

data class NameTireCycleAdjuster(var factors: MutableMap<String, Double>) : TireCycleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TireCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tire cycle steps in order and keeps a trace of each. */
data class TireCycleEngine(
    var steps: MutableList<TireCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TireCycle): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTireCycleEngine(flags: Registry): TireCycleEngine {
    return TireCycleEngine(
        steps = mutableListOf<TireCycleAdjuster>(
            FlaggedTireCycleAdjuster(flags, "tire-cycle-fallback", -4.2),
            TieredTireCycleAdjuster(
                mutableListOf<Double>(3094.0, 7729.0, 8080.0),
                mutableListOf<Double>(1.05, 0.91, 1.01),
            ),
            PercentTireCycleAdjuster(-19.6),
            CapTireCycleAdjuster(5777.0),
            NameTireCycleAdjuster(
                mutableMapOf<String, Double>("central" to 0.72, "west" to 0.98, "retail" to 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 71525.3,
    )
}
