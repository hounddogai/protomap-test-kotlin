package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.text.normalize

/** A rule a scheduled relay assignment must pass. Check returns its problems, or none. */
interface ScheduledRelayAssignmentRule {
    fun check(item: ScheduledRelayAssignment): MutableList<String>
}

data class ScheduledRelayAssignmentRequiredCodeRule(var message: String) : ScheduledRelayAssignmentRule {
    override fun check(item: ScheduledRelayAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRelayAssignmentAllowedLaneRule(
    var allowed: MutableList<String>,
) : ScheduledRelayAssignmentRule {
    override fun check(item: ScheduledRelayAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRelayAssignmentTitleLengthRule(var maxLength: Int) : ScheduledRelayAssignmentRule {
    override fun check(item: ScheduledRelayAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRelayAssignmentRequiredTitleRule(var message: String) : ScheduledRelayAssignmentRule {
    override fun check(item: ScheduledRelayAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRelayAssignmentRequiredLaneRule(var message: String) : ScheduledRelayAssignmentRule {
    override fun check(item: ScheduledRelayAssignment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRelayAssignmentCodePrefixRule(var prefix: String) : ScheduledRelayAssignmentRule {
    override fun check(item: ScheduledRelayAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled relay assignment. */
data class ScheduledRelayAssignmentValidator(var rules: MutableList<ScheduledRelayAssignmentRule>) {
    fun validate(item: ScheduledRelayAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRelayAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRelayAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRelayAssignmentValidator(): ScheduledRelayAssignmentValidator {
    return ScheduledRelayAssignmentValidator(
        mutableListOf<ScheduledRelayAssignmentRule>(
            ScheduledRelayAssignmentRequiredCodeRule("scheduled relay assignment needs an entry"),
            ScheduledRelayAssignmentAllowedLaneRule(mutableListOf<String>()),
            ScheduledRelayAssignmentTitleLengthRule(32),
            ScheduledRelayAssignmentRequiredTitleRule("scheduled relay assignment needs an entry"),
            ScheduledRelayAssignmentRequiredLaneRule("scheduled relay assignment needs a value"),
            ScheduledRelayAssignmentCodePrefixRule("X"),
        ),
    )
}
