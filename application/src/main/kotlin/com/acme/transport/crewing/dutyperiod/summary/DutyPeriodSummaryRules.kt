package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.kit.text.normalize

/** A rule a duty period summary must pass. Check returns its problems, or none. */
interface DutyPeriodSummaryRule {
    fun check(item: DutyPeriodSummary): MutableList<String>
}

data class DutyPeriodSummaryRequiredCodeRule(var message: String) : DutyPeriodSummaryRule {
    override fun check(item: DutyPeriodSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodSummaryExpiresAtRangeRule(var min: Int, var max: Int) : DutyPeriodSummaryRule {
    override fun check(item: DutyPeriodSummary): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodSummaryAllowedTitleRule(var allowed: MutableList<String>) : DutyPeriodSummaryRule {
    override fun check(item: DutyPeriodSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodSummaryCategoryLengthRule(var maxLength: Int) : DutyPeriodSummaryRule {
    override fun check(item: DutyPeriodSummary): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodSummaryCodePrefixRule(var prefix: String) : DutyPeriodSummaryRule {
    override fun check(item: DutyPeriodSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodSummaryUpdatedAtRangeRule(var min: Int, var max: Int) : DutyPeriodSummaryRule {
    override fun check(item: DutyPeriodSummary): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a duty period summary. */
data class DutyPeriodSummaryValidator(var rules: MutableList<DutyPeriodSummaryRule>) {
    fun validate(item: DutyPeriodSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodSummaryValidator(): DutyPeriodSummaryValidator {
    return DutyPeriodSummaryValidator(
        mutableListOf<DutyPeriodSummaryRule>(
            DutyPeriodSummaryRequiredCodeRule("duty period summary needs an entry"),
            DutyPeriodSummaryExpiresAtRangeRule(80, 1000),
            DutyPeriodSummaryAllowedTitleRule(mutableListOf<String>()),
            DutyPeriodSummaryCategoryLengthRule(1000),
            DutyPeriodSummaryCodePrefixRule("X"),
            DutyPeriodSummaryUpdatedAtRangeRule(16, 1000),
        ),
    )
}
