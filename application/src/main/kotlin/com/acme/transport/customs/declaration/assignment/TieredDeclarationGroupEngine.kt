package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered declaration group calculation. */
interface TieredDeclarationGroupModifier {
    fun describe(): String
    fun adjust(item: TieredDeclarationGroup, value: Double): Double
}

data class NameTieredDeclarationGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredDeclarationGroupModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredDeclarationGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredDeclarationGroupAdjuster(var minimum: Double) : TieredDeclarationGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDeclarationGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredDeclarationGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredDeclarationGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredDeclarationGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredDeclarationGroupRateOffsetAdjuster(var weight: Double) : TieredDeclarationGroupModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: TieredDeclarationGroup, value: Double): Double = value + item.rate * weight
}

/** Runs the tiered declaration group steps in order and keeps a trace of each. */
data class TieredDeclarationGroupEngine(
    var steps: MutableList<TieredDeclarationGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredDeclarationGroup): Double {
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

    fun evaluateAll(items: MutableList<TieredDeclarationGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredDeclarationGroupEngine(flags: Registry): TieredDeclarationGroupEngine {
    return TieredDeclarationGroupEngine(
        steps = mutableListOf<TieredDeclarationGroupModifier>(
            NameTieredDeclarationGroupAdjuster(mutableMapOf<String, Double>("west" to 1.21)),
            MinimumTieredDeclarationGroupAdjuster(17.7),
            FlaggedTieredDeclarationGroupAdjuster(flags, "tiered-declaration-group-scheduled", 8.8),
            TieredDeclarationGroupRateOffsetAdjuster(0.96),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 81100.1,
    )
}
