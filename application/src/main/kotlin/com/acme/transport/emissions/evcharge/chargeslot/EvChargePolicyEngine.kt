package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.flags.Registry

/** One step of the ev charge policy calculation. */
interface EvChargePolicyAdjuster {
    fun describe(): String
    fun adjust(item: EvChargePolicy, value: Double): Double
}

data class PercentEvChargePolicyAdjuster(var percent: Double) : EvChargePolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapEvChargePolicyAdjuster(var cap: Double) : EvChargePolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargePolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedEvChargePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargePolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the ev charge policy steps in order and keeps a trace of each. */
data class EvChargePolicyEngine(
    var steps: MutableList<EvChargePolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EvChargePolicy): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargePolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEvChargePolicyEngine(flags: Registry): EvChargePolicyEngine {
    return EvChargePolicyEngine(
        steps = mutableListOf<EvChargePolicyAdjuster>(
            PercentEvChargePolicyAdjuster(17.5),
            CapEvChargePolicyAdjuster(22099.0),
            FlaggedEvChargePolicyAdjuster(flags, "ev-charge-policy-global", -5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 40617.6,
    )
}
