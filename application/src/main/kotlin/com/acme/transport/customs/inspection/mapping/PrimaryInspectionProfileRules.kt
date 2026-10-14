package com.acme.transport.customs.inspection.mapping

import com.acme.transport.kit.text.normalize

/** A rule a primary inspection profile must pass. Check returns its problems, or none. */
interface PrimaryInspectionProfileRule {
    fun check(item: PrimaryInspectionProfile): MutableList<String>
}

data class PrimaryInspectionProfileLabelLengthRule(var maxLength: Int) : PrimaryInspectionProfileRule {
    override fun check(item: PrimaryInspectionProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionProfileDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryInspectionProfileRule {
    override fun check(item: PrimaryInspectionProfile): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class PrimaryInspectionProfileRequiredCodeRule(var message: String) : PrimaryInspectionProfileRule {
    override fun check(item: PrimaryInspectionProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionProfileAllowedLabelRule(
    var allowed: MutableList<String>,
) : PrimaryInspectionProfileRule {
    override fun check(item: PrimaryInspectionProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionProfileStopCountRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryInspectionProfileRule {
    override fun check(item: PrimaryInspectionProfile): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary inspection profile. */
data class PrimaryInspectionProfileValidator(var rules: MutableList<PrimaryInspectionProfileRule>) {
    fun validate(item: PrimaryInspectionProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryInspectionProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryInspectionProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryInspectionProfileValidator(): PrimaryInspectionProfileValidator {
    return PrimaryInspectionProfileValidator(
        mutableListOf<PrimaryInspectionProfileRule>(
            PrimaryInspectionProfileLabelLengthRule(16),
            PrimaryInspectionProfileDurationMinutesRangeRule(3600, 3600),
            PrimaryInspectionProfileRequiredCodeRule("primary inspection profile needs this field"),
            PrimaryInspectionProfileAllowedLabelRule(mutableListOf<String>()),
            PrimaryInspectionProfileStopCountRangeRule(80, 86400),
        ),
    )
}
