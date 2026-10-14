package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.text.normalize

/** A rule a seasonal cutoff assignment must pass. Check returns its problems, or none. */
interface SeasonalCutoffAssignmentRule {
    fun check(item: SeasonalCutoffAssignment): MutableList<String>
}

data class SeasonalCutoffAssignmentCodePrefixRule(var prefix: String) : SeasonalCutoffAssignmentRule {
    override fun check(item: SeasonalCutoffAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCutoffAssignmentAmountRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalCutoffAssignmentRule {
    override fun check(item: SeasonalCutoffAssignment): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class SeasonalCutoffAssignmentCodeLengthRule(var maxLength: Int) : SeasonalCutoffAssignmentRule {
    override fun check(item: SeasonalCutoffAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCutoffAssignmentAllowedLabelRule(
    var allowed: MutableList<String>,
) : SeasonalCutoffAssignmentRule {
    override fun check(item: SeasonalCutoffAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCutoffAssignmentRequiredCodeRule(var message: String) : SeasonalCutoffAssignmentRule {
    override fun check(item: SeasonalCutoffAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal cutoff assignment. */
data class SeasonalCutoffAssignmentValidator(var rules: MutableList<SeasonalCutoffAssignmentRule>) {
    fun validate(item: SeasonalCutoffAssignment): MutableList<String> {
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

    fun requireValid(item: SeasonalCutoffAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCutoffAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCutoffAssignmentValidator(): SeasonalCutoffAssignmentValidator {
    return SeasonalCutoffAssignmentValidator(
        mutableListOf<SeasonalCutoffAssignmentRule>(
            SeasonalCutoffAssignmentCodePrefixRule("X"),
            SeasonalCutoffAssignmentAmountRangeRule(9.6, 163.9),
            SeasonalCutoffAssignmentCodeLengthRule(32),
            SeasonalCutoffAssignmentAllowedLabelRule(mutableListOf<String>("default", "primary")),
            SeasonalCutoffAssignmentRequiredCodeRule("seasonal cutoff assignment needs this field"),
        ),
    )
}
