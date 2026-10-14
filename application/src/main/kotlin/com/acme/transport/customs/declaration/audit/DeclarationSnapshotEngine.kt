package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration snapshot calculation. */
interface DeclarationSnapshotStep {
    fun describe(): String
    fun adjust(item: DeclarationSnapshot, value: Double): Double
}

data class FlaggedDeclarationSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDeclarationSnapshotAdjuster(var step: Double) : DeclarationSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationSnapshot, value: Double): Double = roundTo(value, step)
}

data class TieredDeclarationSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDeclarationSnapshotAdjuster(var cap: Double) : DeclarationSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the declaration snapshot steps in order and keeps a trace of each. */
data class DeclarationSnapshotEngine(
    var steps: MutableList<DeclarationSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationSnapshot): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationSnapshotStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DeclarationSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationSnapshotEngine(flags: Registry): DeclarationSnapshotEngine {
    return DeclarationSnapshotEngine(
        steps = mutableListOf<DeclarationSnapshotStep>(
            FlaggedDeclarationSnapshotAdjuster(flags, "declaration-snapshot-adaptive", -7.9),
            RoundingDeclarationSnapshotAdjuster(0.05),
            TieredDeclarationSnapshotAdjuster(
                mutableListOf<Double>(4710.0, 8435.0),
                mutableListOf<Double>(1.01, 1.08),
            ),
            CapDeclarationSnapshotAdjuster(42561.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 58724.7,
    )
}
