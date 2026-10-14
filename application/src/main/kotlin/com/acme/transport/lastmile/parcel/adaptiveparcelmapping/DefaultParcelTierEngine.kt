package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default parcel tier calculation. */
interface DefaultParcelTierAdjuster {
    fun describe(): String
    fun adjust(item: DefaultParcelTier, value: Double): Double
}

data class FlaggedDefaultParcelTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultParcelTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultParcelTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDefaultParcelTierAdjuster(var minimum: Double) : DefaultParcelTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultParcelTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultParcelTierAdjuster(var cap: Double) : DefaultParcelTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultParcelTier, value: Double): Double = minOf(value, cap)
}

/** Runs the default parcel tier steps in order and keeps a trace of each. */
data class DefaultParcelTierEngine(
    var steps: MutableList<DefaultParcelTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultParcelTier): Double {
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

    fun evaluateAll(items: MutableList<DefaultParcelTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultParcelTierEngine(flags: Registry): DefaultParcelTierEngine {
    return DefaultParcelTierEngine(
        steps = mutableListOf<DefaultParcelTierAdjuster>(
            FlaggedDefaultParcelTierAdjuster(flags, "default-parcel-tier-global", 6.2),
            MinimumDefaultParcelTierAdjuster(8.3),
            CapDefaultParcelTierAdjuster(9602.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 27392.7,
    )
}
