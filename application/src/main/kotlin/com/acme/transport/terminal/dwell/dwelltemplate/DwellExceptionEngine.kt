package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dwell exception calculation. */
interface DwellExceptionAdjuster {
    fun describe(): String
    fun adjust(item: DwellException, value: Double): Double
}

data class PercentDwellExceptionAdjuster(var percent: Double) : DwellExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDwellExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDwellExceptionAdjuster(var minimum: Double) : DwellExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDwellExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the dwell exception steps in order and keeps a trace of each. */
data class DwellExceptionEngine(
    var steps: MutableList<DwellExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DwellException): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDwellExceptionEngine(flags: Registry): DwellExceptionEngine {
    return DwellExceptionEngine(
        steps = mutableListOf<DwellExceptionAdjuster>(
            PercentDwellExceptionAdjuster(-22.7),
            TieredDwellExceptionAdjuster(
                mutableListOf<Double>(1467.0, 3483.0, 9367.0, 9372.0),
                mutableListOf<Double>(0.91, 0.88, 1.01, 0.98),
            ),
            MinimumDwellExceptionAdjuster(1.4),
            FlaggedDwellExceptionAdjuster(flags, "dwell-exception-adaptive", 6.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 14191.5,
    )
}
