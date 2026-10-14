package com.acme.transport.network.cutoff.batch

/** A rule a pending cutoff milestone must pass. Check returns its problems, or none. */
interface PendingCutoffMilestoneRule {
    fun check(item: PendingCutoffMilestone): MutableList<String>
}

data class PendingCutoffMilestoneOdometerRangeRule(var min: Int, var max: Int) : PendingCutoffMilestoneRule {
    override fun check(item: PendingCutoffMilestone): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffMilestoneVersionRangeRule(var min: Int, var max: Int) : PendingCutoffMilestoneRule {
    override fun check(item: PendingCutoffMilestone): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffMilestoneRequiredReviewerRule(var message: String) : PendingCutoffMilestoneRule {
    override fun check(item: PendingCutoffMilestone): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending cutoff milestone. */
data class PendingCutoffMilestoneValidator(var rules: MutableList<PendingCutoffMilestoneRule>) {
    fun validate(item: PendingCutoffMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCutoffMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCutoffMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCutoffMilestoneValidator(): PendingCutoffMilestoneValidator {
    return PendingCutoffMilestoneValidator(
        mutableListOf<PendingCutoffMilestoneRule>(
            PendingCutoffMilestoneOdometerRangeRule(80, 3600),
            PendingCutoffMilestoneVersionRangeRule(16, 32),
            PendingCutoffMilestoneRequiredReviewerRule("pending cutoff milestone needs a value"),
        ),
    )
}
