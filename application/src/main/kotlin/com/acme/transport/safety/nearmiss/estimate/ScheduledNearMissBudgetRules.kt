package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.text.normalize

/** A rule a scheduled near miss budget must pass. Check returns its problems, or none. */
interface ScheduledNearMissBudgetRule {
    fun check(item: ScheduledNearMissBudget): MutableList<String>
}

data class ScheduledNearMissBudgetCreatedAtRangeRule(var min: Int, var max: Int) : ScheduledNearMissBudgetRule {
    override fun check(item: ScheduledNearMissBudget): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissBudgetTitleLengthRule(var maxLength: Int) : ScheduledNearMissBudgetRule {
    override fun check(item: ScheduledNearMissBudget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissBudgetEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledNearMissBudgetRule {
    override fun check(item: ScheduledNearMissBudget): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissBudgetAllowedTitleRule(
    var allowed: MutableList<String>,
) : ScheduledNearMissBudgetRule {
    override fun check(item: ScheduledNearMissBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissBudgetCodePrefixRule(var prefix: String) : ScheduledNearMissBudgetRule {
    override fun check(item: ScheduledNearMissBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNearMissBudgetRequiredCategoryRule(var message: String) : ScheduledNearMissBudgetRule {
    override fun check(item: ScheduledNearMissBudget): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled near miss budget. */
data class ScheduledNearMissBudgetValidator(var rules: MutableList<ScheduledNearMissBudgetRule>) {
    fun validate(item: ScheduledNearMissBudget): MutableList<String> {
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

    fun requireValid(item: ScheduledNearMissBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledNearMissBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledNearMissBudgetValidator(): ScheduledNearMissBudgetValidator {
    return ScheduledNearMissBudgetValidator(
        mutableListOf<ScheduledNearMissBudgetRule>(
            ScheduledNearMissBudgetCreatedAtRangeRule(80, 3600),
            ScheduledNearMissBudgetTitleLengthRule(255),
            ScheduledNearMissBudgetEtaMinutesRangeRule(80, 255),
            ScheduledNearMissBudgetAllowedTitleRule(mutableListOf<String>()),
            ScheduledNearMissBudgetCodePrefixRule("X"),
            ScheduledNearMissBudgetRequiredCategoryRule("scheduled near miss budget needs an entry"),
        ),
    )
}
