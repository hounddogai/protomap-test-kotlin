package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the drop mapping calculation. */
interface DropMappingStep {
    fun describe(): String
    fun adjust(item: DropMapping, value: Double): Double
}

data class FlaggedDropMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDropMappingAdjuster(var percent: Double) : DropMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDropMappingAdjuster(var step: Double) : DropMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropMapping, value: Double): Double = roundTo(value, step)
}

data class MinimumDropMappingAdjuster(var minimum: Double) : DropMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDropMappingAdjuster(var cap: Double) : DropMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropMapping, value: Double): Double = minOf(value, cap)
}

data class DepotDropMappingAdjuster(var factors: MutableMap<String, Double>) : DropMappingStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: DropMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the drop mapping steps in order and keeps a trace of each. */
data class DropMappingEngine(
    var steps: MutableList<DropMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DropMapping): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DropMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDropMappingEngine(flags: Registry): DropMappingEngine {
    return DropMappingEngine(
        steps = mutableListOf<DropMappingStep>(
            FlaggedDropMappingAdjuster(flags, "drop-mapping-manual", 2.8),
            PercentDropMappingAdjuster(11.8),
            RoundingDropMappingAdjuster(5.0),
            MinimumDropMappingAdjuster(41.2),
            CapDropMappingAdjuster(15490.0),
            DepotDropMappingAdjuster(mutableMapOf<String, Double>("online" to 1.01, "west" to 0.98)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 25872.9,
    )
}
