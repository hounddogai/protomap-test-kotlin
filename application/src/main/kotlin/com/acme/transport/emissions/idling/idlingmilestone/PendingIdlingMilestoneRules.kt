package com.acme.transport.emissions.idling.idlingmilestone

/** A rule a pending idling milestone must pass. Check returns its problems, or none. */
interface PendingIdlingMilestoneRule {
    fun check(item: PendingIdlingMilestone): MutableList<String>
}

data class PendingIdlingMilestoneCountryLengthRule(var maxLength: Int) : PendingIdlingMilestoneRule {
    override fun check(item: PendingIdlingMilestone): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingMilestoneRequiredCodeRule(var message: String) : PendingIdlingMilestoneRule {
    override fun check(item: PendingIdlingMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingIdlingMilestoneRequiredTitleRule(var message: String) : PendingIdlingMilestoneRule {
    override fun check(item: PendingIdlingMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending idling milestone. */
data class PendingIdlingMilestoneValidator(var rules: MutableList<PendingIdlingMilestoneRule>) {
    fun validate(item: PendingIdlingMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingIdlingMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingIdlingMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingIdlingMilestoneValidator(): PendingIdlingMilestoneValidator {
    return PendingIdlingMilestoneValidator(
        mutableListOf<PendingIdlingMilestoneRule>(
            PendingIdlingMilestoneCountryLengthRule(16),
            PendingIdlingMilestoneRequiredCodeRule("pending idling milestone needs an entry"),
            PendingIdlingMilestoneRequiredTitleRule("pending idling milestone needs a value"),
        ),
    )
}
