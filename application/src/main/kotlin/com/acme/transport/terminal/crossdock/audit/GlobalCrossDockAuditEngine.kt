package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.flags.Registry

/** One step of the global cross dock audit calculation. */
interface GlobalCrossDockAuditFactor {
    fun describe(): String
    fun adjust(item: GlobalCrossDockAudit, value: Double): Double
}

data class MinimumGlobalCrossDockAuditAdjuster(var minimum: Double) : GlobalCrossDockAuditFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCrossDockAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredGlobalCrossDockAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCrossDockAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCrossDockAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryGlobalCrossDockAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalCrossDockAuditFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: GlobalCrossDockAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentGlobalCrossDockAuditAdjuster(var percent: Double) : GlobalCrossDockAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCrossDockAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalCrossDockAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCrossDockAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCrossDockAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global cross dock audit steps in order and keeps a trace of each. */
data class GlobalCrossDockAuditEngine(
    var steps: MutableList<GlobalCrossDockAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalCrossDockAudit): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalCrossDockAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalCrossDockAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalCrossDockAuditEngine(flags: Registry): GlobalCrossDockAuditEngine {
    return GlobalCrossDockAuditEngine(
        steps = mutableListOf<GlobalCrossDockAuditFactor>(
            MinimumGlobalCrossDockAuditAdjuster(49.6),
            TieredGlobalCrossDockAuditAdjuster(
                mutableListOf<Double>(1051.0, 9751.0),
                mutableListOf<Double>(1.19, 1.12),
            ),
            CategoryGlobalCrossDockAuditAdjuster(
                mutableMapOf<String, Double>("online" to 0.97, "south" to 1.39),
            ),
            PercentGlobalCrossDockAuditAdjuster(-3.8),
            FlaggedGlobalCrossDockAuditAdjuster(flags, "global-cross-dock-audit-rolling", 4.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 96235.5,
    )
}
