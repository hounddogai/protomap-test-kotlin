package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.kit.text.normalize

/** A rule a regional capacity assignment must pass. Check returns its problems, or none. */
interface RegionalCapacityAssignmentRule {
    fun check(item: RegionalCapacityAssignment): MutableList<String>
}

data class RegionalCapacityAssignmentCodePrefixRule(var prefix: String) : RegionalCapacityAssignmentRule {
    override fun check(item: RegionalCapacityAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityAssignmentTitleLengthRule(var maxLength: Int) : RegionalCapacityAssignmentRule {
    override fun check(item: RegionalCapacityAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityAssignmentPriorityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCapacityAssignmentRule {
    override fun check(item: RegionalCapacityAssignment): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class RegionalCapacityAssignmentAllowedDockRule(
    var allowed: MutableList<String>,
) : RegionalCapacityAssignmentRule {
    override fun check(item: RegionalCapacityAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityAssignmentArchivedGuardRule(var limit: Int) : RegionalCapacityAssignmentRule {
    override fun check(item: RegionalCapacityAssignment): MutableList<String> {
        if (item.archived && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityAssignmentRequiredCodeRule(var message: String) : RegionalCapacityAssignmentRule {
    override fun check(item: RegionalCapacityAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional capacity assignment. */
data class RegionalCapacityAssignmentValidator(var rules: MutableList<RegionalCapacityAssignmentRule>) {
    fun validate(item: RegionalCapacityAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCapacityAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCapacityAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCapacityAssignmentValidator(): RegionalCapacityAssignmentValidator {
    return RegionalCapacityAssignmentValidator(
        mutableListOf<RegionalCapacityAssignmentRule>(
            RegionalCapacityAssignmentCodePrefixRule("X"),
            RegionalCapacityAssignmentTitleLengthRule(64),
            RegionalCapacityAssignmentPriorityRangeRule(32, 64),
            RegionalCapacityAssignmentAllowedDockRule(mutableListOf<String>("seasonal", "strict", "primary")),
            RegionalCapacityAssignmentArchivedGuardRule(255),
            RegionalCapacityAssignmentRequiredCodeRule("regional capacity assignment needs an entry"),
        ),
    )
}
