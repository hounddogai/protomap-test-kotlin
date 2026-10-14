package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.kit.text.normalize

/** A rule a lenient inspection slot must pass. Check returns its problems, or none. */
interface LenientInspectionSlotRule {
    fun check(item: LenientInspectionSlot): MutableList<String>
}

data class LenientInspectionSlotCodeLengthRule(var maxLength: Int) : LenientInspectionSlotRule {
    override fun check(item: LenientInspectionSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionSlotAllowedLabelRule(var allowed: MutableList<String>) : LenientInspectionSlotRule {
    override fun check(item: LenientInspectionSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionSlotWeightRangeRule(var min: Double, var max: Double) : LenientInspectionSlotRule {
    override fun check(item: LenientInspectionSlot): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient inspection slot. */
data class LenientInspectionSlotValidator(var rules: MutableList<LenientInspectionSlotRule>) {
    fun validate(item: LenientInspectionSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientInspectionSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientInspectionSlot>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultLenientInspectionSlotValidator(): LenientInspectionSlotValidator {
    return LenientInspectionSlotValidator(
        mutableListOf<LenientInspectionSlotRule>(
            LenientInspectionSlotCodeLengthRule(3600),
            LenientInspectionSlotAllowedLabelRule(mutableListOf<String>("global", "pending")),
            LenientInspectionSlotWeightRangeRule(8.0, 447.2),
        ),
    )
}
