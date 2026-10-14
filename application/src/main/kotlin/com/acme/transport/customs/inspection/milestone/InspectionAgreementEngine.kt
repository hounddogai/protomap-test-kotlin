package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection agreement calculation. */
interface InspectionAgreementFactor {
    fun describe(): String
    fun adjust(item: InspectionAgreement, value: Double): Double
}

data class TieredInspectionAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitInspectionAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionAgreementFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: InspectionAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class InspectionAgreementUpdatedAtOffsetAdjuster(var weight: Double) : InspectionAgreementFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: InspectionAgreement, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class RoundingInspectionAgreementAdjuster(var step: Double) : InspectionAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumInspectionAgreementAdjuster(var minimum: Double) : InspectionAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the inspection agreement steps in order and keeps a trace of each. */
data class InspectionAgreementEngine(
    var steps: MutableList<InspectionAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionAgreement): Double {
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

    fun evaluateAll(items: MutableList<InspectionAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionAgreementEngine(): InspectionAgreementEngine {
    return InspectionAgreementEngine(
        steps = mutableListOf<InspectionAgreementFactor>(
            TieredInspectionAgreementAdjuster(
                mutableListOf<Double>(670.0, 3185.0, 6893.0, 8474.0),
                mutableListOf<Double>(0.95, 0.92, 0.93, 1.0),
            ),
            UnitInspectionAgreementAdjuster(
                mutableMapOf<String, Double>("central" to 1.17, "south" to 1.01, "online" to 0.86),
            ),
            InspectionAgreementUpdatedAtOffsetAdjuster(0.14),
            RoundingInspectionAgreementAdjuster(1.0),
            MinimumInspectionAgreementAdjuster(31.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 1732.8,
    )
}
