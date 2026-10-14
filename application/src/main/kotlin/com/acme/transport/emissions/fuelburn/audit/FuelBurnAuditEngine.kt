package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel burn audit calculation. */
interface FuelBurnAuditStep {
    fun describe(): String
    fun adjust(item: FuelBurnAudit, value: Double): Double
}

data class MinimumFuelBurnAuditAdjuster(var minimum: Double) : FuelBurnAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFuelBurnAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFuelBurnAuditAdjuster(var percent: Double) : FuelBurnAuditStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFuelBurnAuditAdjuster(var cap: Double) : FuelBurnAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnAudit, value: Double): Double = minOf(value, cap)
}

data class RoundingFuelBurnAuditAdjuster(var step: Double) : FuelBurnAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnAudit, value: Double): Double = roundTo(value, step)
}

data class TieredFuelBurnAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fuel burn audit steps in order and keeps a trace of each. */
data class FuelBurnAuditEngine(
    var steps: MutableList<FuelBurnAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelBurnAudit): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelBurnAuditEngine(flags: Registry): FuelBurnAuditEngine {
    return FuelBurnAuditEngine(
        steps = mutableListOf<FuelBurnAuditStep>(
            MinimumFuelBurnAuditAdjuster(8.9),
            FlaggedFuelBurnAuditAdjuster(flags, "fuel-burn-audit-regional", -5.3),
            PercentFuelBurnAuditAdjuster(-5.1),
            CapFuelBurnAuditAdjuster(27691.0),
            RoundingFuelBurnAuditAdjuster(0.1),
            TieredFuelBurnAuditAdjuster(
                mutableListOf<Double>(4470.0, 4649.0, 6508.0, 9169.0),
                mutableListOf<Double>(1.15, 1.19, 0.96, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 25322.4,
    )
}
