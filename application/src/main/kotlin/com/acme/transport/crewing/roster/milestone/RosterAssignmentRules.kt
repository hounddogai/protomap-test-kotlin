package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.text.normalize

/** A rule a roster assignment must pass. Check returns its problems, or none. */
interface RosterAssignmentRule {
    fun check(item: RosterAssignment): MutableList<String>
}

data class RosterAssignmentAllowedCodeRule(var allowed: MutableList<String>) : RosterAssignmentRule {
    override fun check(item: RosterAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RosterAssignmentPriorityRangeRule(var min: Int, var max: Int) : RosterAssignmentRule {
    override fun check(item: RosterAssignment): MutableList<String> {
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

data class RosterAssignmentRequiredNameRule(var message: String) : RosterAssignmentRule {
    override fun check(item: RosterAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterAssignmentTrailerIdLengthRule(var maxLength: Int) : RosterAssignmentRule {
    override fun check(item: RosterAssignment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterAssignmentCodePrefixRule(var prefix: String) : RosterAssignmentRule {
    override fun check(item: RosterAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterAssignmentLabelLengthRule(var maxLength: Int) : RosterAssignmentRule {
    override fun check(item: RosterAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster assignment. */
data class RosterAssignmentValidator(var rules: MutableList<RosterAssignmentRule>) {
    fun validate(item: RosterAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterAssignmentValidator(): RosterAssignmentValidator {
    return RosterAssignmentValidator(
        mutableListOf<RosterAssignmentRule>(
            RosterAssignmentAllowedCodeRule(mutableListOf<String>()),
            RosterAssignmentPriorityRangeRule(120, 3600),
            RosterAssignmentRequiredNameRule("roster assignment needs a value"),
            RosterAssignmentTrailerIdLengthRule(86400),
            RosterAssignmentCodePrefixRule("OPS"),
            RosterAssignmentLabelLengthRule(80),
        ),
    )
}
