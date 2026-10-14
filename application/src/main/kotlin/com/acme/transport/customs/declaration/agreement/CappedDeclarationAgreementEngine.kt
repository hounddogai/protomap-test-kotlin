package com.acme.transport.customs.declaration.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped declaration agreement calculation. */
interface CappedDeclarationAgreementStep {
    fun describe(): String
    fun adjust(item: CappedDeclarationAgreement, value: Double): Double
}

data class TitleCappedDeclarationAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedDeclarationAgreementStep {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedDeclarationAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedDeclarationAgreementAdjuster(var minimum: Double) : CappedDeclarationAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDeclarationAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedDeclarationAgreementAdjuster(var cap: Double) : CappedDeclarationAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDeclarationAgreement, value: Double): Double = minOf(value, cap)
}

data class FlaggedCappedDeclarationAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDeclarationAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDeclarationAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped declaration agreement steps in order and keeps a trace of each. */
data class CappedDeclarationAgreementEngine(
    var steps: MutableList<CappedDeclarationAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedDeclarationAgreement): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedDeclarationAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedDeclarationAgreementEngine(flags: Registry): CappedDeclarationAgreementEngine {
    return CappedDeclarationAgreementEngine(
        steps = mutableListOf<CappedDeclarationAgreementStep>(
            TitleCappedDeclarationAgreementAdjuster(
                mutableMapOf<String, Double>("south" to 1.28, "east" to 1.2),
            ),
            MinimumCappedDeclarationAgreementAdjuster(17.5),
            CapCappedDeclarationAgreementAdjuster(5714.0),
            FlaggedCappedDeclarationAgreementAdjuster(flags, "capped-declaration-agreement-weighted", -3.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 53050.5,
    )
}
