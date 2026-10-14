package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capacity agreement calculation. */
interface CapacityAgreementModifier {
    fun describe(): String
    fun adjust(item: CapacityAgreement, value: Double): Double
}

data class FlaggedCapacityAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityAgreementModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCapacityAgreementAdjuster(var percent: Double) : CapacityAgreementModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCapacityAgreementAdjuster(var cap: Double) : CapacityAgreementModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the capacity agreement steps in order and keeps a trace of each. */
data class CapacityAgreementEngine(
    var steps: MutableList<CapacityAgreementModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CapacityAgreement): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CapacityAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCapacityAgreementEngine(flags: Registry): CapacityAgreementEngine {
    return CapacityAgreementEngine(
        steps = mutableListOf<CapacityAgreementModifier>(
            FlaggedCapacityAgreementAdjuster(flags, "capacity-agreement-batched", -1.0),
            PercentCapacityAgreementAdjuster(18.6),
            CapCapacityAgreementAdjuster(10829.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 17747.6,
    )
}
