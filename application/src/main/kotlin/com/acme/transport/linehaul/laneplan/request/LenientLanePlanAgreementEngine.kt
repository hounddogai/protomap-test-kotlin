package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient lane plan agreement calculation. */
interface LenientLanePlanAgreementStep {
    fun describe(): String
    fun adjust(item: LenientLanePlanAgreement, value: Double): Double
}

data class MinimumLenientLanePlanAgreementAdjuster(var minimum: Double) : LenientLanePlanAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientLanePlanAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleLenientLanePlanAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientLanePlanAgreementStep {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientLanePlanAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientLanePlanAgreementAdjuster(var percent: Double) : LenientLanePlanAgreementStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientLanePlanAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLenientLanePlanAgreementAdjuster(var step: Double) : LenientLanePlanAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientLanePlanAgreement, value: Double): Double = roundTo(value, step)
}

data class TieredLenientLanePlanAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientLanePlanAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientLanePlanAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LenientLanePlanAgreementFloorValueOffsetAdjuster(var weight: Double) : LenientLanePlanAgreementStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: LenientLanePlanAgreement, value: Double): Double = value + item.floorValue * weight
}

/** Runs the lenient lane plan agreement steps in order and keeps a trace of each. */
data class LenientLanePlanAgreementEngine(
    var steps: MutableList<LenientLanePlanAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientLanePlanAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientLanePlanAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientLanePlanAgreementEngine(): LenientLanePlanAgreementEngine {
    return LenientLanePlanAgreementEngine(
        steps = mutableListOf<LenientLanePlanAgreementStep>(
            MinimumLenientLanePlanAgreementAdjuster(30.5),
            TitleLenientLanePlanAgreementAdjuster(
                mutableMapOf<String, Double>("north" to 1.07, "south" to 0.71),
            ),
            PercentLenientLanePlanAgreementAdjuster(-16.8),
            RoundingLenientLanePlanAgreementAdjuster(0.5),
            TieredLenientLanePlanAgreementAdjuster(
                mutableListOf<Double>(7278.0, 8374.0),
                mutableListOf<Double>(0.89, 0.82),
            ),
            LenientLanePlanAgreementFloorValueOffsetAdjuster(1.24),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 66987.3,
    )
}
