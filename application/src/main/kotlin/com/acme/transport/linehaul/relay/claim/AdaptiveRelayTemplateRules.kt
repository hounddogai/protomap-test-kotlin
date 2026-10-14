package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.text.normalize

/** A rule a adaptive relay template must pass. Check returns its problems, or none. */
interface AdaptiveRelayTemplateRule {
    fun check(item: AdaptiveRelayTemplate): MutableList<String>
}

data class AdaptiveRelayTemplateAllowedNameRule(var allowed: MutableList<String>) : AdaptiveRelayTemplateRule {
    override fun check(item: AdaptiveRelayTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayTemplateAmountRangeRule(var min: Double, var max: Double) : AdaptiveRelayTemplateRule {
    override fun check(item: AdaptiveRelayTemplate): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayTemplateRequiredNameRule(var message: String) : AdaptiveRelayTemplateRule {
    override fun check(item: AdaptiveRelayTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRelayTemplateDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveRelayTemplateRule {
    override fun check(item: AdaptiveRelayTemplate): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive relay template. */
data class AdaptiveRelayTemplateValidator(var rules: MutableList<AdaptiveRelayTemplateRule>) {
    fun validate(item: AdaptiveRelayTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveRelayTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRelayTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRelayTemplateValidator(): AdaptiveRelayTemplateValidator {
    return AdaptiveRelayTemplateValidator(
        mutableListOf<AdaptiveRelayTemplateRule>(
            AdaptiveRelayTemplateAllowedNameRule(mutableListOf<String>("lenient", "regional")),
            AdaptiveRelayTemplateAmountRangeRule(6.9, 2634.3),
            AdaptiveRelayTemplateRequiredNameRule("adaptive relay template needs this field"),
            AdaptiveRelayTemplateDurationMinutesRangeRule(16, 3600),
        ),
    )
}
