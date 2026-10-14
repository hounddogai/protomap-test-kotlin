package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.text.normalize

/** A rule a lease return assignment must pass. Check returns its problems, or none. */
interface LeaseReturnAssignmentRule {
    fun check(item: LeaseReturnAssignment): MutableList<String>
}

data class LeaseReturnAssignmentCodePrefixRule(var prefix: String) : LeaseReturnAssignmentRule {
    override fun check(item: LeaseReturnAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAssignmentRequiredLabelRule(var message: String) : LeaseReturnAssignmentRule {
    override fun check(item: LeaseReturnAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAssignmentCostRangeRule(var min: Double, var max: Double) : LeaseReturnAssignmentRule {
    override fun check(item: LeaseReturnAssignment): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnAssignmentDockLengthRule(var maxLength: Int) : LeaseReturnAssignmentRule {
    override fun check(item: LeaseReturnAssignment): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnAssignmentAllowedDepotRule(var allowed: MutableList<String>) : LeaseReturnAssignmentRule {
    override fun check(item: LeaseReturnAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return assignment. */
data class LeaseReturnAssignmentValidator(var rules: MutableList<LeaseReturnAssignmentRule>) {
    fun validate(item: LeaseReturnAssignment): MutableList<String> {
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

    fun requireValid(item: LeaseReturnAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnAssignmentValidator(): LeaseReturnAssignmentValidator {
    return LeaseReturnAssignmentValidator(
        mutableListOf<LeaseReturnAssignmentRule>(
            LeaseReturnAssignmentCodePrefixRule("REF"),
            LeaseReturnAssignmentRequiredLabelRule("lease return assignment needs an entry"),
            LeaseReturnAssignmentCostRangeRule(9.7, 3297.4),
            LeaseReturnAssignmentDockLengthRule(120),
            LeaseReturnAssignmentAllowedDepotRule(mutableListOf<String>()),
        ),
    )
}
