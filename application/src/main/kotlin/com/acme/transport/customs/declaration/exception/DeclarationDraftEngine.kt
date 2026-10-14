package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the declaration draft calculation. */
interface DeclarationDraftModifier {
    fun describe(): String
    fun adjust(item: DeclarationDraft, value: Double): Double
}

data class RoundingDeclarationDraftAdjuster(var step: Double) : DeclarationDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationDraft, value: Double): Double = roundTo(value, step)
}

data class DeclarationDraftQuantityOffsetAdjuster(var weight: Double) : DeclarationDraftModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: DeclarationDraft, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class PercentDeclarationDraftAdjuster(var percent: Double) : DeclarationDraftModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDeclarationDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDeclarationDraftAdjuster(var cap: Double) : DeclarationDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationDraft, value: Double): Double = minOf(value, cap)
}

data class FlaggedDeclarationDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the declaration draft steps in order and keeps a trace of each. */
data class DeclarationDraftEngine(
    var steps: MutableList<DeclarationDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DeclarationDraft): Double {
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

    fun evaluateAll(items: MutableList<DeclarationDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDeclarationDraftEngine(flags: Registry): DeclarationDraftEngine {
    return DeclarationDraftEngine(
        steps = mutableListOf<DeclarationDraftModifier>(
            RoundingDeclarationDraftAdjuster(1.0),
            DeclarationDraftQuantityOffsetAdjuster(1.92),
            PercentDeclarationDraftAdjuster(-19.6),
            TieredDeclarationDraftAdjuster(
                mutableListOf<Double>(185.0, 1641.0, 4794.0, 9835.0),
                mutableListOf<Double>(0.81, 1.12, 0.82, 1.11),
            ),
            CapDeclarationDraftAdjuster(6773.0),
            FlaggedDeclarationDraftAdjuster(flags, "declaration-draft-rolling", 4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 18659.0,
    )
}
