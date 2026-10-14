package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the defect digest calculation. */
interface DefectDigestStep {
    fun describe(): String
    fun adjust(item: DefectDigest, value: Double): Double
}

data class FlaggedDefectDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefectDigestAdjuster(var percent: Double) : DefectDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefectDigestAdjuster(var step: Double) : DefectDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the defect digest steps in order and keeps a trace of each. */
data class DefectDigestEngine(
    var steps: MutableList<DefectDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefectDigest): Double {
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

    fun evaluateAll(items: MutableList<DefectDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefectDigestEngine(flags: Registry): DefectDigestEngine {
    return DefectDigestEngine(
        steps = mutableListOf<DefectDigestStep>(
            FlaggedDefectDigestAdjuster(flags, "defect-digest-weighted", 1.6),
            PercentDefectDigestAdjuster(-8.9),
            RoundingDefectDigestAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 76763.2,
    )
}
