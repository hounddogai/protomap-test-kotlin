package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict cross dock agreement calculation. */
interface StrictCrossDockAgreementStep {
    fun describe(): String
    fun adjust(item: StrictCrossDockAgreement, value: Double): Double
}

data class StrictCrossDockAgreementPriorityOffsetAdjuster(var weight: Double) : StrictCrossDockAgreementStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: StrictCrossDockAgreement, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class CapStrictCrossDockAgreementAdjuster(var cap: Double) : StrictCrossDockAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictCrossDockAgreement, value: Double): Double = minOf(value, cap)
}

data class TieredStrictCrossDockAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictCrossDockAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictCrossDockAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedStrictCrossDockAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictCrossDockAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictCrossDockAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict cross dock agreement steps in order and keeps a trace of each. */
data class StrictCrossDockAgreementEngine(
    var steps: MutableList<StrictCrossDockAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictCrossDockAgreement): Double {
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

    fun evaluateAll(items: MutableList<StrictCrossDockAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictCrossDockAgreementEngine(flags: Registry): StrictCrossDockAgreementEngine {
    return StrictCrossDockAgreementEngine(
        steps = mutableListOf<StrictCrossDockAgreementStep>(
            StrictCrossDockAgreementPriorityOffsetAdjuster(0.77),
            CapStrictCrossDockAgreementAdjuster(20208.0),
            TieredStrictCrossDockAgreementAdjuster(
                mutableListOf<Double>(3177.0, 5024.0),
                mutableListOf<Double>(1.05, 1.13),
            ),
            FlaggedStrictCrossDockAgreementAdjuster(flags, "strict-cross-dock-agreement-capped", 0.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 32527.0,
    )
}
