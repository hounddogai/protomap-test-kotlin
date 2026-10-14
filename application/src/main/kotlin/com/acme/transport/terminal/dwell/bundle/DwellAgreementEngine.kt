package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell agreement calculation. */
interface DwellAgreementStep {
    fun describe(): String
    fun adjust(item: DwellAgreement, value: Double): Double
}

data class RoundingDwellAgreementAdjuster(var step: Double) : DwellAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellAgreement, value: Double): Double = roundTo(value, step)
}

data class LaneDwellAgreementAdjuster(var factors: MutableMap<String, Double>) : DwellAgreementStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: DwellAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDwellAgreementAdjuster(var minimum: Double) : DwellAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDwellAgreementAdjuster(var percent: Double) : DwellAgreementStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DwellAgreementLoadKgOffsetAdjuster(var weight: Double) : DwellAgreementStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: DwellAgreement, value: Double): Double = value + item.loadKg * weight
}

data class CapDwellAgreementAdjuster(var cap: Double) : DwellAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the dwell agreement steps in order and keeps a trace of each. */
data class DwellAgreementEngine(
    var steps: MutableList<DwellAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DwellAgreement): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDwellAgreementEngine(): DwellAgreementEngine {
    return DwellAgreementEngine(
        steps = mutableListOf<DwellAgreementStep>(
            RoundingDwellAgreementAdjuster(0.1),
            LaneDwellAgreementAdjuster(
                mutableMapOf<String, Double>("online" to 1.13, "retail" to 0.76, "central" to 1.32),
            ),
            MinimumDwellAgreementAdjuster(16.6),
            PercentDwellAgreementAdjuster(-19.1),
            DwellAgreementLoadKgOffsetAdjuster(1.75),
            CapDwellAgreementAdjuster(14313.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 62090.3,
    )
}
