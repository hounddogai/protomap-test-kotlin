package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.text.normalize

/** A rule a legacy violation variance must pass. Check returns its problems, or none. */
interface LegacyViolationVarianceRule {
    fun check(item: LegacyViolationVariance): MutableList<String>
}

data class LegacyViolationVarianceRequiredTitleRule(var message: String) : LegacyViolationVarianceRule {
    override fun check(item: LegacyViolationVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyViolationVarianceTitleLengthRule(var maxLength: Int) : LegacyViolationVarianceRule {
    override fun check(item: LegacyViolationVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyViolationVarianceRequiredLabelRule(var message: String) : LegacyViolationVarianceRule {
    override fun check(item: LegacyViolationVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyViolationVarianceCodeLengthRule(var maxLength: Int) : LegacyViolationVarianceRule {
    override fun check(item: LegacyViolationVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyViolationVarianceAllowedTitleRule(
    var allowed: MutableList<String>,
) : LegacyViolationVarianceRule {
    override fun check(item: LegacyViolationVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyViolationVarianceUpdatedAtRangeRule(var min: Int, var max: Int) : LegacyViolationVarianceRule {
    override fun check(item: LegacyViolationVariance): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy violation variance. */
data class LegacyViolationVarianceValidator(var rules: MutableList<LegacyViolationVarianceRule>) {
    fun validate(item: LegacyViolationVariance): MutableList<String> {
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

    fun requireValid(item: LegacyViolationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyViolationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyViolationVarianceValidator(): LegacyViolationVarianceValidator {
    return LegacyViolationVarianceValidator(
        mutableListOf<LegacyViolationVarianceRule>(
            LegacyViolationVarianceRequiredTitleRule("legacy violation variance needs this field"),
            LegacyViolationVarianceTitleLengthRule(16),
            LegacyViolationVarianceRequiredLabelRule("legacy violation variance needs a value"),
            LegacyViolationVarianceCodeLengthRule(64),
            LegacyViolationVarianceAllowedTitleRule(mutableListOf<String>("adaptive")),
            LegacyViolationVarianceUpdatedAtRangeRule(16, 120),
        ),
    )
}
