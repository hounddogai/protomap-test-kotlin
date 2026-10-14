package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.text.normalize

/** A rule a manual qualification target must pass. Check returns its problems, or none. */
interface ManualQualificationTargetRule {
    fun check(item: ManualQualificationTarget): MutableList<String>
}

data class ManualQualificationTargetCodeLengthRule(var maxLength: Int) : ManualQualificationTargetRule {
    override fun check(item: ManualQualificationTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualQualificationTargetMarginRangeRule(
    var min: Double,
    var max: Double,
) : ManualQualificationTargetRule {
    override fun check(item: ManualQualificationTarget): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualQualificationTargetAllowedLabelRule(
    var allowed: MutableList<String>,
) : ManualQualificationTargetRule {
    override fun check(item: ManualQualificationTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual qualification target. */
data class ManualQualificationTargetValidator(var rules: MutableList<ManualQualificationTargetRule>) {
    fun validate(item: ManualQualificationTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualQualificationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualQualificationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultManualQualificationTargetValidator(): ManualQualificationTargetValidator {
    return ManualQualificationTargetValidator(
        mutableListOf<ManualQualificationTargetRule>(
            ManualQualificationTargetCodeLengthRule(16),
            ManualQualificationTargetMarginRangeRule(7.9, 4782.3),
            ManualQualificationTargetAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
