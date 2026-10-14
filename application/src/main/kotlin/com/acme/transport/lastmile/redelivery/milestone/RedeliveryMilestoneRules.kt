package com.acme.transport.lastmile.redelivery.milestone

/** A rule a redelivery milestone must pass. Check returns its problems, or none. */
interface RedeliveryMilestoneRule {
    fun check(item: RedeliveryMilestone): MutableList<String>
}

data class RedeliveryMilestoneRequiredCodeRule(var message: String) : RedeliveryMilestoneRule {
    override fun check(item: RedeliveryMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryMilestoneRequiredNameRule(var message: String) : RedeliveryMilestoneRule {
    override fun check(item: RedeliveryMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryMilestoneRequiredPhoneRule(var message: String) : RedeliveryMilestoneRule {
    override fun check(item: RedeliveryMilestone): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryMilestoneNameLengthRule(var maxLength: Int) : RedeliveryMilestoneRule {
    override fun check(item: RedeliveryMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery milestone. */
data class RedeliveryMilestoneValidator(var rules: MutableList<RedeliveryMilestoneRule>) {
    fun validate(item: RedeliveryMilestone): MutableList<String> {
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

    fun requireValid(item: RedeliveryMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryMilestoneValidator(): RedeliveryMilestoneValidator {
    return RedeliveryMilestoneValidator(
        mutableListOf<RedeliveryMilestoneRule>(
            RedeliveryMilestoneRequiredCodeRule("redelivery milestone needs a value"),
            RedeliveryMilestoneRequiredNameRule("redelivery milestone needs this field"),
            RedeliveryMilestoneRequiredPhoneRule("redelivery milestone needs a value"),
            RedeliveryMilestoneNameLengthRule(80),
        ),
    )
}
