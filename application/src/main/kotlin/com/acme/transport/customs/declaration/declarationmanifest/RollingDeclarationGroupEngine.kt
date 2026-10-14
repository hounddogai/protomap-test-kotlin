package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling declaration group calculation. */
interface RollingDeclarationGroupFactor {
    fun describe(): String
    fun adjust(item: RollingDeclarationGroup, value: Double): Double
}

data class TieredRollingDeclarationGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDeclarationGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDeclarationGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRollingDeclarationGroupAdjuster(var percent: Double) : RollingDeclarationGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDeclarationGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RollingDeclarationGroupBatchSizeOffsetAdjuster(var weight: Double) : RollingDeclarationGroupFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: RollingDeclarationGroup, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class CategoryRollingDeclarationGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingDeclarationGroupFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: RollingDeclarationGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRollingDeclarationGroupAdjuster(var minimum: Double) : RollingDeclarationGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDeclarationGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRollingDeclarationGroupAdjuster(var cap: Double) : RollingDeclarationGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDeclarationGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling declaration group steps in order and keeps a trace of each. */
data class RollingDeclarationGroupEngine(
    var steps: MutableList<RollingDeclarationGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDeclarationGroup): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingDeclarationGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDeclarationGroupEngine(): RollingDeclarationGroupEngine {
    return RollingDeclarationGroupEngine(
        steps = mutableListOf<RollingDeclarationGroupFactor>(
            TieredRollingDeclarationGroupAdjuster(
                mutableListOf<Double>(56.0, 5199.0, 7393.0),
                mutableListOf<Double>(0.9, 1.0, 0.94),
            ),
            PercentRollingDeclarationGroupAdjuster(14.9),
            RollingDeclarationGroupBatchSizeOffsetAdjuster(1.03),
            CategoryRollingDeclarationGroupAdjuster(
                mutableMapOf<String, Double>("online" to 1.11, "east" to 0.94, "north" to 1.18),
            ),
            MinimumRollingDeclarationGroupAdjuster(20.1),
            CapRollingDeclarationGroupAdjuster(27549.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 40276.3,
    )
}
