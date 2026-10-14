package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.text.normalize

/** A rule a global sortation milestone must pass. Check returns its problems, or none. */
interface GlobalSortationMilestoneRule {
    fun check(item: GlobalSortationMilestone): MutableList<String>
}

data class GlobalSortationMilestoneMarginRangeRule(
    var min: Double,
    var max: Double,
) : GlobalSortationMilestoneRule {
    override fun check(item: GlobalSortationMilestone): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSortationMilestoneAllowedNameRule(
    var allowed: MutableList<String>,
) : GlobalSortationMilestoneRule {
    override fun check(item: GlobalSortationMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSortationMilestoneNameLengthRule(var maxLength: Int) : GlobalSortationMilestoneRule {
    override fun check(item: GlobalSortationMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSortationMilestoneCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : GlobalSortationMilestoneRule {
    override fun check(item: GlobalSortationMilestone): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSortationMilestoneCodeLengthRule(var maxLength: Int) : GlobalSortationMilestoneRule {
    override fun check(item: GlobalSortationMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global sortation milestone. */
data class GlobalSortationMilestoneValidator(var rules: MutableList<GlobalSortationMilestoneRule>) {
    fun validate(item: GlobalSortationMilestone): MutableList<String> {
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

    fun requireValid(item: GlobalSortationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalSortationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalSortationMilestoneValidator(): GlobalSortationMilestoneValidator {
    return GlobalSortationMilestoneValidator(
        mutableListOf<GlobalSortationMilestoneRule>(
            GlobalSortationMilestoneMarginRangeRule(4.0, 3654.3),
            GlobalSortationMilestoneAllowedNameRule(mutableListOf<String>("batched", "seasonal", "lenient")),
            GlobalSortationMilestoneNameLengthRule(1000),
            GlobalSortationMilestoneCreatedAtRangeRule(32, 80),
            GlobalSortationMilestoneCodeLengthRule(255),
        ),
    )
}
