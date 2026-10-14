package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered declaration window calculation. */
interface TieredDeclarationWindowStep {
    fun describe(): String
    fun adjust(item: TieredDeclarationWindow, value: Double): Double
}

data class TieredTieredDeclarationWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredDeclarationWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredDeclarationWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTieredDeclarationWindowAdjuster(var percent: Double) : TieredDeclarationWindowStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredDeclarationWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTieredDeclarationWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredDeclarationWindowStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredDeclarationWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTieredDeclarationWindowAdjuster(var step: Double) : TieredDeclarationWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredDeclarationWindow, value: Double): Double = roundTo(value, step)
}

data class TitleTieredDeclarationWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredDeclarationWindowStep {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredDeclarationWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered declaration window steps in order and keeps a trace of each. */
data class TieredDeclarationWindowEngine(
    var steps: MutableList<TieredDeclarationWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredDeclarationWindow): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredDeclarationWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredDeclarationWindowEngine(flags: Registry): TieredDeclarationWindowEngine {
    return TieredDeclarationWindowEngine(
        steps = mutableListOf<TieredDeclarationWindowStep>(
            TieredTieredDeclarationWindowAdjuster(
                mutableListOf<Double>(4110.0, 4982.0),
                mutableListOf<Double>(1.01, 0.95),
            ),
            PercentTieredDeclarationWindowAdjuster(-14.9),
            FlaggedTieredDeclarationWindowAdjuster(flags, "tiered-declaration-window-strict", 9.6),
            RoundingTieredDeclarationWindowAdjuster(0.5),
            TitleTieredDeclarationWindowAdjuster(mutableMapOf<String, Double>("retail" to 0.88, "east" to 0.97)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 18567.7,
    )
}
