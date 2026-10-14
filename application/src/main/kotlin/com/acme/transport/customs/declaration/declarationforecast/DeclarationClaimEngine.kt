package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the declaration claim calculation. */
interface DeclarationClaimModifier {
    fun describe(): String
    fun adjust(item: DeclarationClaim, value: Double): Double
}

data class RoundingDeclarationClaimAdjuster(var step: Double) : DeclarationClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationClaim, value: Double): Double = roundTo(value, step)
}

data class CapDeclarationClaimAdjuster(var cap: Double) : DeclarationClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationClaim, value: Double): Double = minOf(value, cap)
}

data class DeclarationClaimVersionOffsetAdjuster(var weight: Double) : DeclarationClaimModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: DeclarationClaim, value: Double): Double = value + item.version.toDouble() * weight
}

data class LocaleDeclarationClaimAdjuster(var factors: MutableMap<String, Double>) : DeclarationClaimModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: DeclarationClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDeclarationClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the declaration claim steps in order and keeps a trace of each. */
data class DeclarationClaimEngine(
    var steps: MutableList<DeclarationClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DeclarationClaim): Double {
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

    fun evaluateAll(items: MutableList<DeclarationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDeclarationClaimEngine(flags: Registry): DeclarationClaimEngine {
    return DeclarationClaimEngine(
        steps = mutableListOf<DeclarationClaimModifier>(
            RoundingDeclarationClaimAdjuster(1.0),
            CapDeclarationClaimAdjuster(40556.0),
            DeclarationClaimVersionOffsetAdjuster(1.03),
            LocaleDeclarationClaimAdjuster(
                mutableMapOf<String, Double>("central" to 0.95, "retail" to 1.34, "west" to 1.23),
            ),
            FlaggedDeclarationClaimAdjuster(flags, "declaration-claim-lenient", -6.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 80834.9,
    )
}
