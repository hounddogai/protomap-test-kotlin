package com.acme.transport.fleetops.leasereturn.returnmilestone

/** A rule a pending lease return assignment must pass. Check returns its problems, or none. */
interface PendingLeaseReturnAssignmentRule {
    fun check(item: PendingLeaseReturnAssignment): MutableList<String>
}

data class PendingLeaseReturnAssignmentScoreRangeRule(
    var min: Double,
    var max: Double,
) : PendingLeaseReturnAssignmentRule {
    override fun check(item: PendingLeaseReturnAssignment): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingLeaseReturnAssignmentRetriesRangeRule(
    var min: Int,
    var max: Int,
) : PendingLeaseReturnAssignmentRule {
    override fun check(item: PendingLeaseReturnAssignment): MutableList<String> {
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

data class PendingLeaseReturnAssignmentRequiredSourceRule(
    var message: String,
) : PendingLeaseReturnAssignmentRule {
    override fun check(item: PendingLeaseReturnAssignment): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending lease return assignment. */
data class PendingLeaseReturnAssignmentValidator(var rules: MutableList<PendingLeaseReturnAssignmentRule>) {
    fun validate(item: PendingLeaseReturnAssignment): MutableList<String> {
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

    fun requireValid(item: PendingLeaseReturnAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLeaseReturnAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLeaseReturnAssignmentValidator(): PendingLeaseReturnAssignmentValidator {
    return PendingLeaseReturnAssignmentValidator(
        mutableListOf<PendingLeaseReturnAssignmentRule>(
            PendingLeaseReturnAssignmentScoreRangeRule(9.4, 938.4),
            PendingLeaseReturnAssignmentRetriesRangeRule(16, 86400),
            PendingLeaseReturnAssignmentRequiredSourceRule("pending lease return assignment needs a value"),
        ),
    )
}
