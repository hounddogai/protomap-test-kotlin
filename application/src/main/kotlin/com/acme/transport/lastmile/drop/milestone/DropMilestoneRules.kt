package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.kit.text.normalize

/** A rule a drop milestone must pass. Check returns its problems, or none. */
interface DropMilestoneRule {
    fun check(item: DropMilestone): MutableList<String>
}

data class DropMilestoneLocaleLengthRule(var maxLength: Int) : DropMilestoneRule {
    override fun check(item: DropMilestone): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropMilestoneRequiredCodeRule(var message: String) : DropMilestoneRule {
    override fun check(item: DropMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropMilestoneQuantityRangeRule(var min: Int, var max: Int) : DropMilestoneRule {
    override fun check(item: DropMilestone): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropMilestoneNameLengthRule(var maxLength: Int) : DropMilestoneRule {
    override fun check(item: DropMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropMilestoneAllowedRouteCodeRule(var allowed: MutableList<String>) : DropMilestoneRule {
    override fun check(item: DropMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DropMilestoneRequiredRouteCodeRule(var message: String) : DropMilestoneRule {
    override fun check(item: DropMilestone): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop milestone. */
data class DropMilestoneValidator(var rules: MutableList<DropMilestoneRule>) {
    fun validate(item: DropMilestone): MutableList<String> {
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

    fun requireValid(item: DropMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultDropMilestoneValidator(): DropMilestoneValidator {
    return DropMilestoneValidator(
        mutableListOf<DropMilestoneRule>(
            DropMilestoneLocaleLengthRule(32),
            DropMilestoneRequiredCodeRule("drop milestone needs an entry"),
            DropMilestoneQuantityRangeRule(16, 3600),
            DropMilestoneNameLengthRule(3600),
            DropMilestoneAllowedRouteCodeRule(mutableListOf<String>("adaptive", "batched", "strict")),
            DropMilestoneRequiredRouteCodeRule("drop milestone needs this field"),
        ),
    )
}
