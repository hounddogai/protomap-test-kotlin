package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.text.normalize

/** A rule a pending dock door assignment must pass. Check returns its problems, or none. */
interface PendingDockDoorAssignmentRule {
    fun check(item: PendingDockDoorAssignment): MutableList<String>
}

data class PendingDockDoorAssignmentRequiredNameRule(var message: String) : PendingDockDoorAssignmentRule {
    override fun check(item: PendingDockDoorAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorAssignmentDockLengthRule(var maxLength: Int) : PendingDockDoorAssignmentRule {
    override fun check(item: PendingDockDoorAssignment): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorAssignmentAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : PendingDockDoorAssignmentRule {
    override fun check(item: PendingDockDoorAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorAssignmentScoreRangeRule(
    var min: Double,
    var max: Double,
) : PendingDockDoorAssignmentRule {
    override fun check(item: PendingDockDoorAssignment): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorAssignmentRequiredCodeRule(var message: String) : PendingDockDoorAssignmentRule {
    override fun check(item: PendingDockDoorAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending dock door assignment. */
data class PendingDockDoorAssignmentValidator(var rules: MutableList<PendingDockDoorAssignmentRule>) {
    fun validate(item: PendingDockDoorAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDockDoorAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDockDoorAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDockDoorAssignmentValidator(): PendingDockDoorAssignmentValidator {
    return PendingDockDoorAssignmentValidator(
        mutableListOf<PendingDockDoorAssignmentRule>(
            PendingDockDoorAssignmentRequiredNameRule("pending dock door assignment needs a value"),
            PendingDockDoorAssignmentDockLengthRule(32),
            PendingDockDoorAssignmentAllowedOwnerIdRule(mutableListOf<String>()),
            PendingDockDoorAssignmentScoreRangeRule(6.4, 2111.6),
            PendingDockDoorAssignmentRequiredCodeRule("pending dock door assignment needs an entry"),
        ),
    )
}
