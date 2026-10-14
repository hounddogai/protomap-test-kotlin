package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling dock door agreement calculation. */
interface RollingDockDoorAgreementFactor {
    fun describe(): String
    fun adjust(item: RollingDockDoorAgreement, value: Double): Double
}

data class RollingDockDoorAgreementFloorValueOffsetAdjuster(
    var weight: Double,
) : RollingDockDoorAgreementFactor {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: RollingDockDoorAgreement, value: Double): Double = value + item.floorValue * weight
}

data class RoundingRollingDockDoorAgreementAdjuster(var step: Double) : RollingDockDoorAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDockDoorAgreement, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingDockDoorAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDockDoorAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDockDoorAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling dock door agreement steps in order and keeps a trace of each. */
data class RollingDockDoorAgreementEngine(
    var steps: MutableList<RollingDockDoorAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingDockDoorAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDockDoorAgreementFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDockDoorAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingDockDoorAgreementEngine(flags: Registry): RollingDockDoorAgreementEngine {
    return RollingDockDoorAgreementEngine(
        steps = mutableListOf<RollingDockDoorAgreementFactor>(
            RollingDockDoorAgreementFloorValueOffsetAdjuster(1.06),
            RoundingRollingDockDoorAgreementAdjuster(0.05),
            FlaggedRollingDockDoorAgreementAdjuster(flags, "rolling-dock-door-agreement-scheduled", -4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 22274.4,
    )
}
