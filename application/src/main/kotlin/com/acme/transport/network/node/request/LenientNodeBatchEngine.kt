package com.acme.transport.network.node.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient node batch calculation. */
interface LenientNodeBatchModifier {
    fun describe(): String
    fun adjust(item: LenientNodeBatch, value: Double): Double
}

data class FlaggedLenientNodeBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientNodeBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientNodeBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLenientNodeBatchAdjuster(var step: Double) : LenientNodeBatchModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientNodeBatch, value: Double): Double = roundTo(value, step)
}

data class CapLenientNodeBatchAdjuster(var cap: Double) : LenientNodeBatchModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientNodeBatch, value: Double): Double = minOf(value, cap)
}

data class SourceLenientNodeBatchAdjuster(var factors: MutableMap<String, Double>) : LenientNodeBatchModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: LenientNodeBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientNodeBatchAdjuster(var percent: Double) : LenientNodeBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientNodeBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient node batch steps in order and keeps a trace of each. */
data class LenientNodeBatchEngine(
    var steps: MutableList<LenientNodeBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientNodeBatch): Double {
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

    fun evaluateAll(items: MutableList<LenientNodeBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientNodeBatchEngine(flags: Registry): LenientNodeBatchEngine {
    return LenientNodeBatchEngine(
        steps = mutableListOf<LenientNodeBatchModifier>(
            FlaggedLenientNodeBatchAdjuster(flags, "lenient-node-batch-rolling", 4.1),
            RoundingLenientNodeBatchAdjuster(5.0),
            CapLenientNodeBatchAdjuster(20686.0),
            SourceLenientNodeBatchAdjuster(mutableMapOf<String, Double>("west" to 0.89)),
            PercentLenientNodeBatchAdjuster(3.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 80359.7,
    )
}
