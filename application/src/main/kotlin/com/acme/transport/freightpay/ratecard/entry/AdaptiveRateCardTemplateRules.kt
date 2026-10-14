package com.acme.transport.freightpay.ratecard.entry

/** A rule a adaptive rate card template must pass. Check returns its problems, or none. */
interface AdaptiveRateCardTemplateRule {
    fun check(item: AdaptiveRateCardTemplate): MutableList<String>
}

data class AdaptiveRateCardTemplateCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveRateCardTemplateRule {
    override fun check(item: AdaptiveRateCardTemplate): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveRateCardTemplateRequiredCodeRule(var message: String) : AdaptiveRateCardTemplateRule {
    override fun check(item: AdaptiveRateCardTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardTemplateTitleLengthRule(var maxLength: Int) : AdaptiveRateCardTemplateRule {
    override fun check(item: AdaptiveRateCardTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive rate card template. */
data class AdaptiveRateCardTemplateValidator(var rules: MutableList<AdaptiveRateCardTemplateRule>) {
    fun validate(item: AdaptiveRateCardTemplate): MutableList<String> {
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

    fun requireValid(item: AdaptiveRateCardTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRateCardTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRateCardTemplateValidator(): AdaptiveRateCardTemplateValidator {
    return AdaptiveRateCardTemplateValidator(
        mutableListOf<AdaptiveRateCardTemplateRule>(
            AdaptiveRateCardTemplateCreatedAtRangeRule(16, 86400),
            AdaptiveRateCardTemplateRequiredCodeRule("adaptive rate card template needs this field"),
            AdaptiveRateCardTemplateTitleLengthRule(16),
        ),
    )
}
