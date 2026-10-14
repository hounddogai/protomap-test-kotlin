package com.acme.transport.customs.tariff.milestone

/** A rule a adaptive tariff milestone must pass. Check returns its problems, or none. */
interface AdaptiveTariffMilestoneRule {
    fun check(item: AdaptiveTariffMilestone): MutableList<String>
}

data class AdaptiveTariffMilestoneLabelLengthRule(var maxLength: Int) : AdaptiveTariffMilestoneRule {
    override fun check(item: AdaptiveTariffMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffMilestoneCodeLengthRule(var maxLength: Int) : AdaptiveTariffMilestoneRule {
    override fun check(item: AdaptiveTariffMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffMilestoneRequiredCodeRule(var message: String) : AdaptiveTariffMilestoneRule {
    override fun check(item: AdaptiveTariffMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffMilestoneRequiredDriverIdRule(var message: String) : AdaptiveTariffMilestoneRule {
    override fun check(item: AdaptiveTariffMilestone): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffMilestoneRouteCodeLengthRule(var maxLength: Int) : AdaptiveTariffMilestoneRule {
    override fun check(item: AdaptiveTariffMilestone): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffMilestoneVersionRangeRule(var min: Int, var max: Int) : AdaptiveTariffMilestoneRule {
    override fun check(item: AdaptiveTariffMilestone): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive tariff milestone. */
data class AdaptiveTariffMilestoneValidator(var rules: MutableList<AdaptiveTariffMilestoneRule>) {
    fun validate(item: AdaptiveTariffMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveTariffMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTariffMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTariffMilestoneValidator(): AdaptiveTariffMilestoneValidator {
    return AdaptiveTariffMilestoneValidator(
        mutableListOf<AdaptiveTariffMilestoneRule>(
            AdaptiveTariffMilestoneLabelLengthRule(1000),
            AdaptiveTariffMilestoneCodeLengthRule(120),
            AdaptiveTariffMilestoneRequiredCodeRule("adaptive tariff milestone needs an entry"),
            AdaptiveTariffMilestoneRequiredDriverIdRule("adaptive tariff milestone needs an entry"),
            AdaptiveTariffMilestoneRouteCodeLengthRule(1000),
            AdaptiveTariffMilestoneVersionRangeRule(255, 255),
        ),
    )
}
