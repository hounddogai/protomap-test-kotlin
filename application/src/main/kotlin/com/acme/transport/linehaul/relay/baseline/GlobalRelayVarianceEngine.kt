package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global relay variance calculation. */
interface GlobalRelayVarianceFactor {
    fun describe(): String
    fun adjust(item: GlobalRelayVariance, value: Double): Double
}

data class RoundingGlobalRelayVarianceAdjuster(var step: Double) : GlobalRelayVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRelayVariance, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalRelayVarianceAdjuster(var percent: Double) : GlobalRelayVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRelayVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalRelayVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRelayVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRelayVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global relay variance steps in order and keeps a trace of each. */
data class GlobalRelayVarianceEngine(
    var steps: MutableList<GlobalRelayVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalRelayVariance): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalRelayVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalRelayVarianceEngine(flags: Registry): GlobalRelayVarianceEngine {
    return GlobalRelayVarianceEngine(
        steps = mutableListOf<GlobalRelayVarianceFactor>(
            RoundingGlobalRelayVarianceAdjuster(0.5),
            PercentGlobalRelayVarianceAdjuster(-15.2),
            FlaggedGlobalRelayVarianceAdjuster(flags, "global-relay-variance-pending", -9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 16863.6,
    )
}
