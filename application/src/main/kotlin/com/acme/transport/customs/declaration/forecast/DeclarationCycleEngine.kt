package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the declaration cycle calculation. */
interface DeclarationCycleAdjuster {
    fun describe(): String
    fun adjust(item: DeclarationCycle, value: Double): Double
}

data class CapDeclarationCycleAdjuster(var cap: Double) : DeclarationCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationCycle, value: Double): Double = minOf(value, cap)
}

data class SourceDeclarationCycleAdjuster(var factors: MutableMap<String, Double>) : DeclarationCycleAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: DeclarationCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDeclarationCycleAdjuster(var minimum: Double) : DeclarationCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DeclarationCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDeclarationCycleAdjuster(var percent: Double) : DeclarationCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDeclarationCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the declaration cycle steps in order and keeps a trace of each. */
data class DeclarationCycleEngine(
    var steps: MutableList<DeclarationCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DeclarationCycle): Double {
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

    fun evaluateAll(items: MutableList<DeclarationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDeclarationCycleEngine(flags: Registry): DeclarationCycleEngine {
    return DeclarationCycleEngine(
        steps = mutableListOf<DeclarationCycleAdjuster>(
            CapDeclarationCycleAdjuster(26449.0),
            SourceDeclarationCycleAdjuster(mutableMapOf<String, Double>("west" to 0.87, "south" to 1.19)),
            MinimumDeclarationCycleAdjuster(31.5),
            PercentDeclarationCycleAdjuster(10.0),
            FlaggedDeclarationCycleAdjuster(flags, "declaration-cycle-legacy", 6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 55518.4,
    )
}
