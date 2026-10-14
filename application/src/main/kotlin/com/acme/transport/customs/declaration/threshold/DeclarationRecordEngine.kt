package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.flags.Registry

/** One step of the declaration record calculation. */
interface DeclarationRecordModifier {
    fun describe(): String
    fun adjust(item: DeclarationRecord, value: Double): Double
}

data class FlaggedDeclarationRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DeclarationRecordRetriesOffsetAdjuster(var weight: Double) : DeclarationRecordModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: DeclarationRecord, value: Double): Double = value + item.retries.toDouble() * weight
}

data class LabelDeclarationRecordAdjuster(var factors: MutableMap<String, Double>) : DeclarationRecordModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: DeclarationRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDeclarationRecordAdjuster(var cap: Double) : DeclarationRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationRecord, value: Double): Double = minOf(value, cap)
}

/** Runs the declaration record steps in order and keeps a trace of each. */
data class DeclarationRecordEngine(
    var steps: MutableList<DeclarationRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DeclarationRecord): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDeclarationRecordEngine(flags: Registry): DeclarationRecordEngine {
    return DeclarationRecordEngine(
        steps = mutableListOf<DeclarationRecordModifier>(
            FlaggedDeclarationRecordAdjuster(flags, "declaration-record-primary", -2.6),
            DeclarationRecordRetriesOffsetAdjuster(0.11),
            LabelDeclarationRecordAdjuster(
                mutableMapOf<String, Double>("online" to 1.03, "east" to 1.25, "south" to 0.92),
            ),
            CapDeclarationRecordAdjuster(21104.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 95221.5,
    )
}
