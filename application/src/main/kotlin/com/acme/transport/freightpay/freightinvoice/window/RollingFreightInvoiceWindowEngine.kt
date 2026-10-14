package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling freight invoice window calculation. */
interface RollingFreightInvoiceWindowModifier {
    fun describe(): String
    fun adjust(item: RollingFreightInvoiceWindow, value: Double): Double
}

data class RoundingRollingFreightInvoiceWindowAdjuster(var step: Double) : RollingFreightInvoiceWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingFreightInvoiceWindow, value: Double): Double = roundTo(value, step)
}

data class TieredRollingFreightInvoiceWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingFreightInvoiceWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingFreightInvoiceWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingFreightInvoiceWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingFreightInvoiceWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingFreightInvoiceWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling freight invoice window steps in order and keeps a trace of each. */
data class RollingFreightInvoiceWindowEngine(
    var steps: MutableList<RollingFreightInvoiceWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingFreightInvoiceWindow): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingFreightInvoiceWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingFreightInvoiceWindowEngine(flags: Registry): RollingFreightInvoiceWindowEngine {
    return RollingFreightInvoiceWindowEngine(
        steps = mutableListOf<RollingFreightInvoiceWindowModifier>(
            RoundingRollingFreightInvoiceWindowAdjuster(5.0),
            TieredRollingFreightInvoiceWindowAdjuster(
                mutableListOf<Double>(5908.0, 6627.0, 9101.0),
                mutableListOf<Double>(0.92, 0.96, 1.07),
            ),
            FlaggedRollingFreightInvoiceWindowAdjuster(flags, "rolling-freight-invoice-window-global", 2.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 59117.0,
    )
}
