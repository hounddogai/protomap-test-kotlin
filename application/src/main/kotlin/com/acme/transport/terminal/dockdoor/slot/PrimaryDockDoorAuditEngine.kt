package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.flags.Registry

/** One step of the primary dock door audit calculation. */
interface PrimaryDockDoorAuditAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryDockDoorAudit, value: Double): Double
}

data class ChannelPrimaryDockDoorAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDockDoorAuditAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: PrimaryDockDoorAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimaryDockDoorAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDockDoorAuditAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDockDoorAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPrimaryDockDoorAuditAdjuster(var percent: Double) : PrimaryDockDoorAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDockDoorAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary dock door audit steps in order and keeps a trace of each. */
data class PrimaryDockDoorAuditEngine(
    var steps: MutableList<PrimaryDockDoorAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryDockDoorAudit): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDockDoorAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDockDoorAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryDockDoorAuditEngine(flags: Registry): PrimaryDockDoorAuditEngine {
    return PrimaryDockDoorAuditEngine(
        steps = mutableListOf<PrimaryDockDoorAuditAdjuster>(
            ChannelPrimaryDockDoorAuditAdjuster(mutableMapOf<String, Double>("online" to 1.15, "south" to 1.05)),
            FlaggedPrimaryDockDoorAuditAdjuster(flags, "primary-dock-door-audit-strict", 1.6),
            PercentPrimaryDockDoorAuditAdjuster(-21.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 23624.7,
    )
}
