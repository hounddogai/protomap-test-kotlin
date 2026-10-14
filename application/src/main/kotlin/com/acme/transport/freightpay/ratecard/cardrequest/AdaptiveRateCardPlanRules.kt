package com.acme.transport.freightpay.ratecard.cardrequest

/** A rule a adaptive rate card plan must pass. Check returns its problems, or none. */
interface AdaptiveRateCardPlanRule {
    fun check(item: AdaptiveRateCardPlan): MutableList<String>
}

data class AdaptiveRateCardPlanScoreRangeRule(var min: Double, var max: Double) : AdaptiveRateCardPlanRule {
    override fun check(item: AdaptiveRateCardPlan): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardPlanCodeLengthRule(var maxLength: Int) : AdaptiveRateCardPlanRule {
    override fun check(item: AdaptiveRateCardPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardPlanRequiredCodeRule(var message: String) : AdaptiveRateCardPlanRule {
    override fun check(item: AdaptiveRateCardPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardPlanRequiredNameRule(var message: String) : AdaptiveRateCardPlanRule {
    override fun check(item: AdaptiveRateCardPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardPlanCodePrefixRule(var prefix: String) : AdaptiveRateCardPlanRule {
    override fun check(item: AdaptiveRateCardPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardPlanRetriesRangeRule(var min: Int, var max: Int) : AdaptiveRateCardPlanRule {
    override fun check(item: AdaptiveRateCardPlan): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive rate card plan. */
data class AdaptiveRateCardPlanValidator(var rules: MutableList<AdaptiveRateCardPlanRule>) {
    fun validate(item: AdaptiveRateCardPlan): MutableList<String> {
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

    fun requireValid(item: AdaptiveRateCardPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRateCardPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRateCardPlanValidator(): AdaptiveRateCardPlanValidator {
    return AdaptiveRateCardPlanValidator(
        mutableListOf<AdaptiveRateCardPlanRule>(
            AdaptiveRateCardPlanScoreRangeRule(9.7, 2058.7),
            AdaptiveRateCardPlanCodeLengthRule(3600),
            AdaptiveRateCardPlanRequiredCodeRule("adaptive rate card plan needs an entry"),
            AdaptiveRateCardPlanRequiredNameRule("adaptive rate card plan needs a value"),
            AdaptiveRateCardPlanCodePrefixRule("OPS"),
            AdaptiveRateCardPlanRetriesRangeRule(32, 64),
        ),
    )
}
