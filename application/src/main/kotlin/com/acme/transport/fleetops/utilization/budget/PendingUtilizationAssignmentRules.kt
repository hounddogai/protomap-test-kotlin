package com.acme.transport.fleetops.utilization.budget

/** A rule a pending utilization assignment must pass. Check returns its problems, or none. */
interface PendingUtilizationAssignmentRule {
    fun check(item: PendingUtilizationAssignment): MutableList<String>
}

data class PendingUtilizationAssignmentRequiredLabelRule(
    var message: String,
) : PendingUtilizationAssignmentRule {
    override fun check(item: PendingUtilizationAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingUtilizationAssignmentRequiredOwnerIdRule(
    var message: String,
) : PendingUtilizationAssignmentRule {
    override fun check(item: PendingUtilizationAssignment): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingUtilizationAssignmentRequiredCodeRule(
    var message: String,
) : PendingUtilizationAssignmentRule {
    override fun check(item: PendingUtilizationAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingUtilizationAssignmentCodePrefixRule(var prefix: String) : PendingUtilizationAssignmentRule {
    override fun check(item: PendingUtilizationAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending utilization assignment. */
data class PendingUtilizationAssignmentValidator(var rules: MutableList<PendingUtilizationAssignmentRule>) {
    fun validate(item: PendingUtilizationAssignment): MutableList<String> {
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

    fun requireValid(item: PendingUtilizationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingUtilizationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingUtilizationAssignmentValidator(): PendingUtilizationAssignmentValidator {
    return PendingUtilizationAssignmentValidator(
        mutableListOf<PendingUtilizationAssignmentRule>(
            PendingUtilizationAssignmentRequiredLabelRule("pending utilization assignment needs an entry"),
            PendingUtilizationAssignmentRequiredOwnerIdRule("pending utilization assignment needs an entry"),
            PendingUtilizationAssignmentRequiredCodeRule("pending utilization assignment needs an entry"),
            PendingUtilizationAssignmentCodePrefixRule("REF"),
        ),
    )
}
