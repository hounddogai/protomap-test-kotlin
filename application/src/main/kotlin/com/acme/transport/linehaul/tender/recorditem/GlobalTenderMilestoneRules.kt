package com.acme.transport.linehaul.tender.recorditem

/** A rule a global tender milestone must pass. Check returns its problems, or none. */
interface GlobalTenderMilestoneRule {
    fun check(item: GlobalTenderMilestone): MutableList<String>
}

data class GlobalTenderMilestoneChannelLengthRule(var maxLength: Int) : GlobalTenderMilestoneRule {
    override fun check(item: GlobalTenderMilestone): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderMilestoneCodePrefixRule(var prefix: String) : GlobalTenderMilestoneRule {
    override fun check(item: GlobalTenderMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderMilestoneRequiredLabelRule(var message: String) : GlobalTenderMilestoneRule {
    override fun check(item: GlobalTenderMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderMilestoneLabelLengthRule(var maxLength: Int) : GlobalTenderMilestoneRule {
    override fun check(item: GlobalTenderMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tender milestone. */
data class GlobalTenderMilestoneValidator(var rules: MutableList<GlobalTenderMilestoneRule>) {
    fun validate(item: GlobalTenderMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTenderMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTenderMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTenderMilestoneValidator(): GlobalTenderMilestoneValidator {
    return GlobalTenderMilestoneValidator(
        mutableListOf<GlobalTenderMilestoneRule>(
            GlobalTenderMilestoneChannelLengthRule(86400),
            GlobalTenderMilestoneCodePrefixRule("ACM"),
            GlobalTenderMilestoneRequiredLabelRule("global tender milestone needs this field"),
            GlobalTenderMilestoneLabelLengthRule(64),
        ),
    )
}
