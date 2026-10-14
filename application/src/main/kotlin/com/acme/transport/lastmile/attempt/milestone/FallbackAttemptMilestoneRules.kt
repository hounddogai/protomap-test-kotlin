package com.acme.transport.lastmile.attempt.milestone

/** A rule a fallback attempt milestone must pass. Check returns its problems, or none. */
interface FallbackAttemptMilestoneRule {
    fun check(item: FallbackAttemptMilestone): MutableList<String>
}

data class FallbackAttemptMilestoneArchivedGuardRule(var limit: Int) : FallbackAttemptMilestoneRule {
    override fun check(item: FallbackAttemptMilestone): MutableList<String> {
        if (item.archived && item.graceDays > limit) {
            return mutableListOf<String>("grace days ${item.graceDays} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAttemptMilestoneRequiredDescriptionRule(var message: String) : FallbackAttemptMilestoneRule {
    override fun check(item: FallbackAttemptMilestone): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAttemptMilestoneCodeLengthRule(var maxLength: Int) : FallbackAttemptMilestoneRule {
    override fun check(item: FallbackAttemptMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback attempt milestone. */
data class FallbackAttemptMilestoneValidator(var rules: MutableList<FallbackAttemptMilestoneRule>) {
    fun validate(item: FallbackAttemptMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackAttemptMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAttemptMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAttemptMilestoneValidator(): FallbackAttemptMilestoneValidator {
    return FallbackAttemptMilestoneValidator(
        mutableListOf<FallbackAttemptMilestoneRule>(
            FallbackAttemptMilestoneArchivedGuardRule(3600),
            FallbackAttemptMilestoneRequiredDescriptionRule("fallback attempt milestone needs this field"),
            FallbackAttemptMilestoneCodeLengthRule(64),
        ),
    )
}
