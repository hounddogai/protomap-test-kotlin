package com.acme.transport.fleetops.defect.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the defect agreement calculation. */
interface DefectAgreementFactor {
    fun describe(): String
    fun adjust(item: DefectAgreement, value: Double): Double
}

data class TieredDefectAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDefectAgreementAdjuster(var percent: Double) : DefectAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefectAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefectAgreementAdjuster(var cap: Double) : DefectAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectAgreement, value: Double): Double = minOf(value, cap)
}

data class RoundingDefectAgreementAdjuster(var step: Double) : DefectAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectAgreement, value: Double): Double = roundTo(value, step)
}

/** Runs the defect agreement steps in order and keeps a trace of each. */
data class DefectAgreementEngine(
    var steps: MutableList<DefectAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefectAgreement): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefectAgreementEngine(flags: Registry): DefectAgreementEngine {
    return DefectAgreementEngine(
        steps = mutableListOf<DefectAgreementFactor>(
            TieredDefectAgreementAdjuster(
                mutableListOf<Double>(4823.0, 8540.0),
                mutableListOf<Double>(0.88, 1.13),
            ),
            PercentDefectAgreementAdjuster(1.2),
            FlaggedDefectAgreementAdjuster(flags, "defect-agreement-batched", -0.8),
            CapDefectAgreementAdjuster(5939.0),
            RoundingDefectAgreementAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 35580.6,
    )
}
