package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual declaration batch calculation. */
interface ManualDeclarationBatchStep {
    fun describe(): String
    fun adjust(item: ManualDeclarationBatch, value: Double): Double
}

data class RoundingManualDeclarationBatchAdjuster(var step: Double) : ManualDeclarationBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDeclarationBatch, value: Double): Double = roundTo(value, step)
}

data class PercentManualDeclarationBatchAdjuster(var percent: Double) : ManualDeclarationBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDeclarationBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualDeclarationBatchAdjuster(var cap: Double) : ManualDeclarationBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDeclarationBatch, value: Double): Double = minOf(value, cap)
}

data class ManualDeclarationBatchEtaMinutesOffsetAdjuster(var weight: Double) : ManualDeclarationBatchStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: ManualDeclarationBatch, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class ChannelManualDeclarationBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDeclarationBatchStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: ManualDeclarationBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualDeclarationBatchAdjuster(var minimum: Double) : ManualDeclarationBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDeclarationBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual declaration batch steps in order and keeps a trace of each. */
data class ManualDeclarationBatchEngine(
    var steps: MutableList<ManualDeclarationBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualDeclarationBatch): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualDeclarationBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualDeclarationBatchEngine(): ManualDeclarationBatchEngine {
    return ManualDeclarationBatchEngine(
        steps = mutableListOf<ManualDeclarationBatchStep>(
            RoundingManualDeclarationBatchAdjuster(1.0),
            PercentManualDeclarationBatchAdjuster(11.5),
            CapManualDeclarationBatchAdjuster(27010.0),
            ManualDeclarationBatchEtaMinutesOffsetAdjuster(1.16),
            ChannelManualDeclarationBatchAdjuster(mutableMapOf<String, Double>("retail" to 1.02)),
            MinimumManualDeclarationBatchAdjuster(0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 67818.6,
    )
}
