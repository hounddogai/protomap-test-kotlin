package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.text.normalize

/** A rule a rest break assignment must pass. Check returns its problems, or none. */
interface RestBreakAssignmentRule {
    fun check(item: RestBreakAssignment): MutableList<String>
}

data class RestBreakAssignmentApprovedGuardRule(var limit: Int) : RestBreakAssignmentRule {
    override fun check(item: RestBreakAssignment): MutableList<String> {
        if (item.approved && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAssignmentVersionRangeRule(var min: Int, var max: Int) : RestBreakAssignmentRule {
    override fun check(item: RestBreakAssignment): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAssignmentLimitRangeRule(var min: Int, var max: Int) : RestBreakAssignmentRule {
    override fun check(item: RestBreakAssignment): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAssignmentAllowedCodeRule(var allowed: MutableList<String>) : RestBreakAssignmentRule {
    override fun check(item: RestBreakAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAssignmentRequiredLabelRule(var message: String) : RestBreakAssignmentRule {
    override fun check(item: RestBreakAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAssignmentCodeLengthRule(var maxLength: Int) : RestBreakAssignmentRule {
    override fun check(item: RestBreakAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break assignment. */
data class RestBreakAssignmentValidator(var rules: MutableList<RestBreakAssignmentRule>) {
    fun validate(item: RestBreakAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakAssignmentValidator(): RestBreakAssignmentValidator {
    return RestBreakAssignmentValidator(
        mutableListOf<RestBreakAssignmentRule>(
            RestBreakAssignmentApprovedGuardRule(16),
            RestBreakAssignmentVersionRangeRule(80, 1000),
            RestBreakAssignmentLimitRangeRule(80, 86400),
            RestBreakAssignmentAllowedCodeRule(mutableListOf<String>()),
            RestBreakAssignmentRequiredLabelRule("rest break assignment needs an entry"),
            RestBreakAssignmentCodeLengthRule(32),
        ),
    )
}
