package com.acme.transport.fleetops.leasereturn.profile

/** A rule a scheduled lease return profile must pass. Check returns its problems, or none. */
interface ScheduledLeaseReturnProfileRule {
    fun check(item: ScheduledLeaseReturnProfile): MutableList<String>
}

data class ScheduledLeaseReturnProfileRequiredCodeRule(var message: String) : ScheduledLeaseReturnProfileRule {
    override fun check(item: ScheduledLeaseReturnProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLeaseReturnProfilePriorityRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledLeaseReturnProfileRule {
    override fun check(item: ScheduledLeaseReturnProfile): MutableList<String> {
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

data class ScheduledLeaseReturnProfileTitleLengthRule(var maxLength: Int) : ScheduledLeaseReturnProfileRule {
    override fun check(item: ScheduledLeaseReturnProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled lease return profile. */
data class ScheduledLeaseReturnProfileValidator(var rules: MutableList<ScheduledLeaseReturnProfileRule>) {
    fun validate(item: ScheduledLeaseReturnProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledLeaseReturnProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLeaseReturnProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLeaseReturnProfileValidator(): ScheduledLeaseReturnProfileValidator {
    return ScheduledLeaseReturnProfileValidator(
        mutableListOf<ScheduledLeaseReturnProfileRule>(
            ScheduledLeaseReturnProfileRequiredCodeRule("scheduled lease return profile needs a value"),
            ScheduledLeaseReturnProfilePriorityRangeRule(64, 255),
            ScheduledLeaseReturnProfileTitleLengthRule(120),
        ),
    )
}
