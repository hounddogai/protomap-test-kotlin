package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell digest calculation. */
interface DwellDigestModifier {
    fun describe(): String
    fun adjust(item: DwellDigest, value: Double): Double
}

data class PercentDwellDigestAdjuster(var percent: Double) : DwellDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDwellDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDwellDigestAdjuster(var step: Double) : DwellDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the dwell digest steps in order and keeps a trace of each. */
data class DwellDigestEngine(
    var steps: MutableList<DwellDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DwellDigest): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDwellDigestEngine(flags: Registry): DwellDigestEngine {
    return DwellDigestEngine(
        steps = mutableListOf<DwellDigestModifier>(
            PercentDwellDigestAdjuster(18.9),
            FlaggedDwellDigestAdjuster(flags, "dwell-digest-fallback", 1.0),
            RoundingDwellDigestAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 21371.1,
    )
}
