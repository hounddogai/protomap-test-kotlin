package com.acme.transport.fleetops.utilization.reservation

/** A rule a utilization assignment must pass. Check returns its problems, or none. */
interface UtilizationAssignmentRule {
    fun check(item: UtilizationAssignment): MutableList<String>
}

data class UtilizationAssignmentRequiredTitleRule(var message: String) : UtilizationAssignmentRule {
    override fun check(item: UtilizationAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAssignmentCeilingRangeRule(var min: Double, var max: Double) : UtilizationAssignmentRule {
    override fun check(item: UtilizationAssignment): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAssignmentPriorityRangeRule(var min: Int, var max: Int) : UtilizationAssignmentRule {
    override fun check(item: UtilizationAssignment): MutableList<String> {
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

data class UtilizationAssignmentApprovedGuardRule(var limit: Int) : UtilizationAssignmentRule {
    override fun check(item: UtilizationAssignment): MutableList<String> {
        if (item.approved && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAssignmentDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : UtilizationAssignmentRule {
    override fun check(item: UtilizationAssignment): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a utilization assignment. */
data class UtilizationAssignmentValidator(var rules: MutableList<UtilizationAssignmentRule>) {
    fun validate(item: UtilizationAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationAssignmentValidator(): UtilizationAssignmentValidator {
    return UtilizationAssignmentValidator(
        mutableListOf<UtilizationAssignmentRule>(
            UtilizationAssignmentRequiredTitleRule("utilization assignment needs this field"),
            UtilizationAssignmentCeilingRangeRule(0.9, 3712.0),
            UtilizationAssignmentPriorityRangeRule(64, 80),
            UtilizationAssignmentApprovedGuardRule(16),
            UtilizationAssignmentDurationMinutesRangeRule(1000, 86400),
        ),
    )
}
