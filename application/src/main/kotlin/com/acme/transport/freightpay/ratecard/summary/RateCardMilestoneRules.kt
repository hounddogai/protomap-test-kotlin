package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.text.normalize

/** A rule a rate card milestone must pass. Check returns its problems, or none. */
interface RateCardMilestoneRule {
    fun check(item: RateCardMilestone): MutableList<String>
}

data class RateCardMilestoneCodeLengthRule(var maxLength: Int) : RateCardMilestoneRule {
    override fun check(item: RateCardMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardMilestoneRequiredCodeRule(var message: String) : RateCardMilestoneRule {
    override fun check(item: RateCardMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardMilestoneReviewerLengthRule(var maxLength: Int) : RateCardMilestoneRule {
    override fun check(item: RateCardMilestone): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardMilestoneEtaMinutesRangeRule(var min: Int, var max: Int) : RateCardMilestoneRule {
    override fun check(item: RateCardMilestone): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardMilestoneCeilingRangeRule(var min: Double, var max: Double) : RateCardMilestoneRule {
    override fun check(item: RateCardMilestone): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RateCardMilestoneAllowedNameRule(var allowed: MutableList<String>) : RateCardMilestoneRule {
    override fun check(item: RateCardMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card milestone. */
data class RateCardMilestoneValidator(var rules: MutableList<RateCardMilestoneRule>) {
    fun validate(item: RateCardMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardMilestoneValidator(): RateCardMilestoneValidator {
    return RateCardMilestoneValidator(
        mutableListOf<RateCardMilestoneRule>(
            RateCardMilestoneCodeLengthRule(255),
            RateCardMilestoneRequiredCodeRule("rate card milestone needs an entry"),
            RateCardMilestoneReviewerLengthRule(3600),
            RateCardMilestoneEtaMinutesRangeRule(16, 32),
            RateCardMilestoneCeilingRangeRule(3.2, 4546.5),
            RateCardMilestoneAllowedNameRule(mutableListOf<String>("regional")),
        ),
    )
}
