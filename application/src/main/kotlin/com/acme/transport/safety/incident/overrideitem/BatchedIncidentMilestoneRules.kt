package com.acme.transport.safety.incident.overrideitem

/** A rule a batched incident milestone must pass. Check returns its problems, or none. */
interface BatchedIncidentMilestoneRule {
    fun check(item: BatchedIncidentMilestone): MutableList<String>
}

data class BatchedIncidentMilestoneWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : BatchedIncidentMilestoneRule {
    override fun check(item: BatchedIncidentMilestone): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class BatchedIncidentMilestoneActiveGuardRule(var limit: Double) : BatchedIncidentMilestoneRule {
    override fun check(item: BatchedIncidentMilestone): MutableList<String> {
        if (item.active && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIncidentMilestoneRequiredCodeRule(var message: String) : BatchedIncidentMilestoneRule {
    override fun check(item: BatchedIncidentMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedIncidentMilestoneTitleLengthRule(var maxLength: Int) : BatchedIncidentMilestoneRule {
    override fun check(item: BatchedIncidentMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched incident milestone. */
data class BatchedIncidentMilestoneValidator(var rules: MutableList<BatchedIncidentMilestoneRule>) {
    fun validate(item: BatchedIncidentMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedIncidentMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedIncidentMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedIncidentMilestoneValidator(): BatchedIncidentMilestoneValidator {
    return BatchedIncidentMilestoneValidator(
        mutableListOf<BatchedIncidentMilestoneRule>(
            BatchedIncidentMilestoneWindowDaysRangeRule(32, 3600),
            BatchedIncidentMilestoneActiveGuardRule(1.3),
            BatchedIncidentMilestoneRequiredCodeRule("batched incident milestone needs this field"),
            BatchedIncidentMilestoneTitleLengthRule(32),
        ),
    )
}
