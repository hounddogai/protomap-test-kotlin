package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.text.normalize

/** A rule a regional redelivery assignment must pass. Check returns its problems, or none. */
interface RegionalRedeliveryAssignmentRule {
    fun check(item: RegionalRedeliveryAssignment): MutableList<String>
}

data class RegionalRedeliveryAssignmentNameLengthRule(var maxLength: Int) : RegionalRedeliveryAssignmentRule {
    override fun check(item: RegionalRedeliveryAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryAssignmentAllowedNameRule(
    var allowed: MutableList<String>,
) : RegionalRedeliveryAssignmentRule {
    override fun check(item: RegionalRedeliveryAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryAssignmentRetriesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalRedeliveryAssignmentRule {
    override fun check(item: RegionalRedeliveryAssignment): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional redelivery assignment. */
data class RegionalRedeliveryAssignmentValidator(var rules: MutableList<RegionalRedeliveryAssignmentRule>) {
    fun validate(item: RegionalRedeliveryAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalRedeliveryAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRedeliveryAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRedeliveryAssignmentValidator(): RegionalRedeliveryAssignmentValidator {
    return RegionalRedeliveryAssignmentValidator(
        mutableListOf<RegionalRedeliveryAssignmentRule>(
            RegionalRedeliveryAssignmentNameLengthRule(16),
            RegionalRedeliveryAssignmentAllowedNameRule(mutableListOf<String>()),
            RegionalRedeliveryAssignmentRetriesRangeRule(120, 1000),
        ),
    )
}
