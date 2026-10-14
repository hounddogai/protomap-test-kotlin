package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global fuel surcharge exception calculation. */
interface GlobalFuelSurchargeExceptionModifier {
    fun describe(): String
    fun adjust(item: GlobalFuelSurchargeException, value: Double): Double
}

data class MinimumGlobalFuelSurchargeExceptionAdjuster(
    var minimum: Double,
) : GlobalFuelSurchargeExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalFuelSurchargeException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalFuelSurchargeExceptionFloorValueOffsetAdjuster(
    var weight: Double,
) : GlobalFuelSurchargeExceptionModifier {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: GlobalFuelSurchargeException, value: Double): Double = value + item.floorValue * weight
}

data class FlaggedGlobalFuelSurchargeExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalFuelSurchargeExceptionModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalFuelSurchargeException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global fuel surcharge exception steps in order and keeps a trace of each. */
data class GlobalFuelSurchargeExceptionEngine(
    var steps: MutableList<GlobalFuelSurchargeExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalFuelSurchargeException): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalFuelSurchargeException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalFuelSurchargeExceptionEngine(flags: Registry): GlobalFuelSurchargeExceptionEngine {
    return GlobalFuelSurchargeExceptionEngine(
        steps = mutableListOf<GlobalFuelSurchargeExceptionModifier>(
            MinimumGlobalFuelSurchargeExceptionAdjuster(2.6),
            GlobalFuelSurchargeExceptionFloorValueOffsetAdjuster(1.88),
            FlaggedGlobalFuelSurchargeExceptionAdjuster(flags, "global-fuel-surcharge-exception-lenient", -2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 73488.8,
    )
}
