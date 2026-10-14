package com.acme.transport.customs.declaration.declarationmanifest

/** One step of the rolling declaration record calculation. */
interface RollingDeclarationRecordStep {
    fun describe(): String
    fun adjust(item: RollingDeclarationRecord, value: Double): Double
}

data class TieredRollingDeclarationRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDeclarationRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDeclarationRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceRollingDeclarationRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingDeclarationRecordStep {
    override fun describe(): String = "by source"

    override fun adjust(item: RollingDeclarationRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRollingDeclarationRecordAdjuster(var cap: Double) : RollingDeclarationRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDeclarationRecord, value: Double): Double = minOf(value, cap)
}

data class RollingDeclarationRecordLoadKgOffsetAdjuster(var weight: Double) : RollingDeclarationRecordStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: RollingDeclarationRecord, value: Double): Double = value + item.loadKg * weight
}

/** Runs the rolling declaration record steps in order and keeps a trace of each. */
data class RollingDeclarationRecordEngine(
    var steps: MutableList<RollingDeclarationRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingDeclarationRecord): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingDeclarationRecordStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingDeclarationRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingDeclarationRecordEngine(): RollingDeclarationRecordEngine {
    return RollingDeclarationRecordEngine(
        steps = mutableListOf<RollingDeclarationRecordStep>(
            TieredRollingDeclarationRecordAdjuster(
                mutableListOf<Double>(673.0, 3831.0),
                mutableListOf<Double>(1.19, 0.84),
            ),
            SourceRollingDeclarationRecordAdjuster(
                mutableMapOf<String, Double>("online" to 1.13, "retail" to 1.03, "east" to 0.83),
            ),
            CapRollingDeclarationRecordAdjuster(36824.0),
            RollingDeclarationRecordLoadKgOffsetAdjuster(1.92),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 86136.1,
    )
}
