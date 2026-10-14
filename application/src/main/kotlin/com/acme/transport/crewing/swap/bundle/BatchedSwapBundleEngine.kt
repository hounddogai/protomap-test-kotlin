package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.flags.Registry

/** One step of the batched swap bundle calculation. */
interface BatchedSwapBundleStep {
    fun describe(): String
    fun adjust(item: BatchedSwapBundle, value: Double): Double
}

data class FlaggedBatchedSwapBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedSwapBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedSwapBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBatchedSwapBundleAdjuster(var cap: Double) : BatchedSwapBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedSwapBundle, value: Double): Double = minOf(value, cap)
}

data class TieredBatchedSwapBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedSwapBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedSwapBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the batched swap bundle steps in order and keeps a trace of each. */
data class BatchedSwapBundleEngine(
    var steps: MutableList<BatchedSwapBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedSwapBundle): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedSwapBundleStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedSwapBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedSwapBundleEngine(flags: Registry): BatchedSwapBundleEngine {
    return BatchedSwapBundleEngine(
        steps = mutableListOf<BatchedSwapBundleStep>(
            FlaggedBatchedSwapBundleAdjuster(flags, "batched-swap-bundle-seasonal", 7.5),
            CapBatchedSwapBundleAdjuster(43779.0),
            TieredBatchedSwapBundleAdjuster(
                mutableListOf<Double>(881.0, 2449.0, 4096.0),
                mutableListOf<Double>(0.81, 1.06, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 25044.7,
    )
}
