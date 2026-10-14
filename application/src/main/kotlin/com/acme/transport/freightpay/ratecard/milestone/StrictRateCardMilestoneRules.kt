package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.text.normalize

/** A rule a strict rate card milestone must pass. Check returns its problems, or none. */
interface StrictRateCardMilestoneRule {
    fun check(item: StrictRateCardMilestone): MutableList<String>
}

data class StrictRateCardMilestoneAllowedDepotRule(
    var allowed: MutableList<String>,
) : StrictRateCardMilestoneRule {
    override fun check(item: StrictRateCardMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardMilestoneAmountRangeRule(
    var min: Double,
    var max: Double,
) : StrictRateCardMilestoneRule {
    override fun check(item: StrictRateCardMilestone): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardMilestoneDepotLengthRule(var maxLength: Int) : StrictRateCardMilestoneRule {
    override fun check(item: StrictRateCardMilestone): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardMilestoneVehicleIdLengthRule(var maxLength: Int) : StrictRateCardMilestoneRule {
    override fun check(item: StrictRateCardMilestone): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardMilestoneLabelLengthRule(var maxLength: Int) : StrictRateCardMilestoneRule {
    override fun check(item: StrictRateCardMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardMilestoneRequiredVehicleIdRule(var message: String) : StrictRateCardMilestoneRule {
    override fun check(item: StrictRateCardMilestone): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rate card milestone. */
data class StrictRateCardMilestoneValidator(var rules: MutableList<StrictRateCardMilestoneRule>) {
    fun validate(item: StrictRateCardMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRateCardMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRateCardMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRateCardMilestoneValidator(): StrictRateCardMilestoneValidator {
    return StrictRateCardMilestoneValidator(
        mutableListOf<StrictRateCardMilestoneRule>(
            StrictRateCardMilestoneAllowedDepotRule(mutableListOf<String>("strict")),
            StrictRateCardMilestoneAmountRangeRule(2.2, 399.1),
            StrictRateCardMilestoneDepotLengthRule(32),
            StrictRateCardMilestoneVehicleIdLengthRule(64),
            StrictRateCardMilestoneLabelLengthRule(86400),
            StrictRateCardMilestoneRequiredVehicleIdRule("strict rate card milestone needs a value"),
        ),
    )
}
