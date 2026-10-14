package com.acme.transport.freightpay.ratecard.audit

/** A rule a fallback rate card template must pass. Check returns its problems, or none. */
interface FallbackRateCardTemplateRule {
    fun check(item: FallbackRateCardTemplate): MutableList<String>
}

data class FallbackRateCardTemplateMaximumRangeRule(
    var min: Double,
    var max: Double,
) : FallbackRateCardTemplateRule {
    override fun check(item: FallbackRateCardTemplate): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class FallbackRateCardTemplateRequiredLabelRule(var message: String) : FallbackRateCardTemplateRule {
    override fun check(item: FallbackRateCardTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardTemplateRequiredExternalRefRule(var message: String) : FallbackRateCardTemplateRule {
    override fun check(item: FallbackRateCardTemplate): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRateCardTemplateLabelLengthRule(var maxLength: Int) : FallbackRateCardTemplateRule {
    override fun check(item: FallbackRateCardTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback rate card template. */
data class FallbackRateCardTemplateValidator(var rules: MutableList<FallbackRateCardTemplateRule>) {
    fun validate(item: FallbackRateCardTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRateCardTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRateCardTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRateCardTemplateValidator(): FallbackRateCardTemplateValidator {
    return FallbackRateCardTemplateValidator(
        mutableListOf<FallbackRateCardTemplateRule>(
            FallbackRateCardTemplateMaximumRangeRule(7.7, 2874.1),
            FallbackRateCardTemplateRequiredLabelRule("fallback rate card template needs a value"),
            FallbackRateCardTemplateRequiredExternalRefRule("fallback rate card template needs an entry"),
            FallbackRateCardTemplateLabelLengthRule(32),
        ),
    )
}
