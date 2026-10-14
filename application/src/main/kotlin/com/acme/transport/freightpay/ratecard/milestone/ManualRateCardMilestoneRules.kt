package com.acme.transport.freightpay.ratecard.milestone

/** A rule a manual rate card milestone must pass. Check returns its problems, or none. */
interface ManualRateCardMilestoneRule {
    fun check(item: ManualRateCardMilestone): MutableList<String>
}

data class ManualRateCardMilestoneRequiredCodeRule(var message: String) : ManualRateCardMilestoneRule {
    override fun check(item: ManualRateCardMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardMilestoneLabelLengthRule(var maxLength: Int) : ManualRateCardMilestoneRule {
    override fun check(item: ManualRateCardMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRateCardMilestoneCodePrefixRule(var prefix: String) : ManualRateCardMilestoneRule {
    override fun check(item: ManualRateCardMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual rate card milestone. */
data class ManualRateCardMilestoneValidator(var rules: MutableList<ManualRateCardMilestoneRule>) {
    fun validate(item: ManualRateCardMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRateCardMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRateCardMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRateCardMilestoneValidator(): ManualRateCardMilestoneValidator {
    return ManualRateCardMilestoneValidator(
        mutableListOf<ManualRateCardMilestoneRule>(
            ManualRateCardMilestoneRequiredCodeRule("manual rate card milestone needs a value"),
            ManualRateCardMilestoneLabelLengthRule(1000),
            ManualRateCardMilestoneCodePrefixRule("ACM"),
        ),
    )
}
