package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback dwell bundle calculation. */
interface FallbackDwellBundleFactor {
    fun describe(): String
    fun adjust(item: FallbackDwellBundle, value: Double): Double
}

data class RoundingFallbackDwellBundleAdjuster(var step: Double) : FallbackDwellBundleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackDwellBundle, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackDwellBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDwellBundleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDwellBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackDwellBundleAdjuster(var cap: Double) : FallbackDwellBundleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDwellBundle, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback dwell bundle steps in order and keeps a trace of each. */
data class FallbackDwellBundleEngine(
    var steps: MutableList<FallbackDwellBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDwellBundle): Double {
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

    fun evaluateAll(items: MutableList<FallbackDwellBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDwellBundleEngine(flags: Registry): FallbackDwellBundleEngine {
    return FallbackDwellBundleEngine(
        steps = mutableListOf<FallbackDwellBundleFactor>(
            RoundingFallbackDwellBundleAdjuster(0.5),
            FlaggedFallbackDwellBundleAdjuster(flags, "fallback-dwell-bundle-batched", 2.5),
            CapFallbackDwellBundleAdjuster(42883.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 7633.8,
    )
}
