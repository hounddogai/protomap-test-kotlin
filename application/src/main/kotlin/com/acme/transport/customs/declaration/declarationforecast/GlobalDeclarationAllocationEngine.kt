package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global declaration allocation calculation. */
interface GlobalDeclarationAllocationModifier {
    fun describe(): String
    fun adjust(item: GlobalDeclarationAllocation, value: Double): Double
}

data class PercentGlobalDeclarationAllocationAdjuster(
    var percent: Double,
) : GlobalDeclarationAllocationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDeclarationAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredGlobalDeclarationAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalDeclarationAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalDeclarationAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingGlobalDeclarationAllocationAdjuster(var step: Double) : GlobalDeclarationAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalDeclarationAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalDeclarationAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalDeclarationAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalDeclarationAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global declaration allocation steps in order and keeps a trace of each. */
data class GlobalDeclarationAllocationEngine(
    var steps: MutableList<GlobalDeclarationAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalDeclarationAllocation): Double {
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

    fun evaluateAll(items: MutableList<GlobalDeclarationAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalDeclarationAllocationEngine(flags: Registry): GlobalDeclarationAllocationEngine {
    return GlobalDeclarationAllocationEngine(
        steps = mutableListOf<GlobalDeclarationAllocationModifier>(
            PercentGlobalDeclarationAllocationAdjuster(-8.0),
            TieredGlobalDeclarationAllocationAdjuster(
                mutableListOf<Double>(1157.0, 1409.0, 5691.0, 5980.0),
                mutableListOf<Double>(0.9, 0.95, 0.98, 0.94),
            ),
            RoundingGlobalDeclarationAllocationAdjuster(5.0),
            FlaggedGlobalDeclarationAllocationAdjuster(flags, "global-declaration-allocation-lenient", 8.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 25890.7,
    )
}
