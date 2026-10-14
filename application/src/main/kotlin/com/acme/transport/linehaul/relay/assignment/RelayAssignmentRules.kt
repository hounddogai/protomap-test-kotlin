package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.text.normalize

/** A rule a relay assignment must pass. Check returns its problems, or none. */
interface RelayAssignmentRule {
    fun check(item: RelayAssignment): MutableList<String>
}

data class RelayAssignmentAllowedRouteCodeRule(var allowed: MutableList<String>) : RelayAssignmentRule {
    override fun check(item: RelayAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RelayAssignmentRequiredCodeRule(var message: String) : RelayAssignmentRule {
    override fun check(item: RelayAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayAssignmentSourceLengthRule(var maxLength: Int) : RelayAssignmentRule {
    override fun check(item: RelayAssignment): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayAssignmentTitleLengthRule(var maxLength: Int) : RelayAssignmentRule {
    override fun check(item: RelayAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayAssignmentRequiredTitleRule(var message: String) : RelayAssignmentRule {
    override fun check(item: RelayAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayAssignmentNotesLengthRule(var maxLength: Int) : RelayAssignmentRule {
    override fun check(item: RelayAssignment): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay assignment. */
data class RelayAssignmentValidator(var rules: MutableList<RelayAssignmentRule>) {
    fun validate(item: RelayAssignment): MutableList<String> {
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

    fun requireValid(item: RelayAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayAssignmentValidator(): RelayAssignmentValidator {
    return RelayAssignmentValidator(
        mutableListOf<RelayAssignmentRule>(
            RelayAssignmentAllowedRouteCodeRule(mutableListOf<String>("rolling", "strict", "adaptive")),
            RelayAssignmentRequiredCodeRule("relay assignment needs this field"),
            RelayAssignmentSourceLengthRule(80),
            RelayAssignmentTitleLengthRule(32),
            RelayAssignmentRequiredTitleRule("relay assignment needs an entry"),
            RelayAssignmentNotesLengthRule(255),
        ),
    )
}
