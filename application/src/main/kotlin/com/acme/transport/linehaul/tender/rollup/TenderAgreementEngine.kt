package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tender agreement calculation. */
interface TenderAgreementStep {
    fun describe(): String
    fun adjust(item: TenderAgreement, value: Double): Double
}

data class FlaggedTenderAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTenderAgreementAdjuster(var minimum: Double) : TenderAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTenderAgreementAdjuster(var cap: Double) : TenderAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the tender agreement steps in order and keeps a trace of each. */
data class TenderAgreementEngine(
    var steps: MutableList<TenderAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TenderAgreement): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TenderAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTenderAgreementEngine(flags: Registry): TenderAgreementEngine {
    return TenderAgreementEngine(
        steps = mutableListOf<TenderAgreementStep>(
            FlaggedTenderAgreementAdjuster(flags, "tender-agreement-strict", 5.2),
            MinimumTenderAgreementAdjuster(5.4),
            CapTenderAgreementAdjuster(32997.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 92899.0,
    )
}
