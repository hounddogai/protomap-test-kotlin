package com.acme.transport.lastmile.locker.rule

/** A rule a locker milestone must pass. Check returns its problems, or none. */
interface LockerMilestoneRule {
    fun check(item: LockerMilestone): MutableList<String>
}

data class LockerMilestoneReviewerLengthRule(var maxLength: Int) : LockerMilestoneRule {
    override fun check(item: LockerMilestone): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerMilestoneRequiredCodeRule(var message: String) : LockerMilestoneRule {
    override fun check(item: LockerMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerMilestoneTitleLengthRule(var maxLength: Int) : LockerMilestoneRule {
    override fun check(item: LockerMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker milestone. */
data class LockerMilestoneValidator(var rules: MutableList<LockerMilestoneRule>) {
    fun validate(item: LockerMilestone): MutableList<String> {
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

    fun requireValid(item: LockerMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerMilestoneValidator(): LockerMilestoneValidator {
    return LockerMilestoneValidator(
        mutableListOf<LockerMilestoneRule>(
            LockerMilestoneReviewerLengthRule(16),
            LockerMilestoneRequiredCodeRule("locker milestone needs an entry"),
            LockerMilestoneTitleLengthRule(32),
        ),
    )
}
