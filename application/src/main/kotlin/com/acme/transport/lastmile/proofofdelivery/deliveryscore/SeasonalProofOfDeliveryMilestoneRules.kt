package com.acme.transport.lastmile.proofofdelivery.deliveryscore

/** A rule a seasonal proof of delivery milestone must pass. Check returns its problems, or none. */
interface SeasonalProofOfDeliveryMilestoneRule {
    fun check(item: SeasonalProofOfDeliveryMilestone): MutableList<String>
}

data class SeasonalProofOfDeliveryMilestoneCodeLengthRule(
    var maxLength: Int,
) : SeasonalProofOfDeliveryMilestoneRule {
    override fun check(item: SeasonalProofOfDeliveryMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryMilestoneRequiredCodeRule(
    var message: String,
) : SeasonalProofOfDeliveryMilestoneRule {
    override fun check(item: SeasonalProofOfDeliveryMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryMilestoneRequiredNameRule(
    var message: String,
) : SeasonalProofOfDeliveryMilestoneRule {
    override fun check(item: SeasonalProofOfDeliveryMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryMilestoneCodePrefixRule(
    var prefix: String,
) : SeasonalProofOfDeliveryMilestoneRule {
    override fun check(item: SeasonalProofOfDeliveryMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryMilestoneRatioRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalProofOfDeliveryMilestoneRule {
    override fun check(item: SeasonalProofOfDeliveryMilestone): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryMilestoneMinimumRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalProofOfDeliveryMilestoneRule {
    override fun check(item: SeasonalProofOfDeliveryMilestone): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal proof of delivery milestone. */
data class SeasonalProofOfDeliveryMilestoneValidator(
    var rules: MutableList<SeasonalProofOfDeliveryMilestoneRule>,
) {
    fun validate(item: SeasonalProofOfDeliveryMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalProofOfDeliveryMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalProofOfDeliveryMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalProofOfDeliveryMilestoneValidator(): SeasonalProofOfDeliveryMilestoneValidator {
    return SeasonalProofOfDeliveryMilestoneValidator(
        mutableListOf<SeasonalProofOfDeliveryMilestoneRule>(
            SeasonalProofOfDeliveryMilestoneCodeLengthRule(3600),
            SeasonalProofOfDeliveryMilestoneRequiredCodeRule(
                "seasonal proof of delivery milestone needs an entry",
            ),
            SeasonalProofOfDeliveryMilestoneRequiredNameRule(
                "seasonal proof of delivery milestone needs an entry",
            ),
            SeasonalProofOfDeliveryMilestoneCodePrefixRule("X"),
            SeasonalProofOfDeliveryMilestoneRatioRangeRule(6.3, 337.7),
            SeasonalProofOfDeliveryMilestoneMinimumRangeRule(5.5, 1241.7),
        ),
    )
}
