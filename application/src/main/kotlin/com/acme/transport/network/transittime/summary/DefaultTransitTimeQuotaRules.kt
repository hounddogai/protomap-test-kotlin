package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.text.normalize

/** A rule a default transit time quota must pass. Check returns its problems, or none. */
interface DefaultTransitTimeQuotaRule {
    fun check(item: DefaultTransitTimeQuota): MutableList<String>
}

data class DefaultTransitTimeQuotaAllowedCountryRule(
    var allowed: MutableList<String>,
) : DefaultTransitTimeQuotaRule {
    override fun check(item: DefaultTransitTimeQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeQuotaCountryLengthRule(var maxLength: Int) : DefaultTransitTimeQuotaRule {
    override fun check(item: DefaultTransitTimeQuota): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeQuotaBudgetRangeRule(
    var min: Double,
    var max: Double,
) : DefaultTransitTimeQuotaRule {
    override fun check(item: DefaultTransitTimeQuota): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class DefaultTransitTimeQuotaRequiredCodeRule(var message: String) : DefaultTransitTimeQuotaRule {
    override fun check(item: DefaultTransitTimeQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeQuotaFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : DefaultTransitTimeQuotaRule {
    override fun check(item: DefaultTransitTimeQuota): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default transit time quota. */
data class DefaultTransitTimeQuotaValidator(var rules: MutableList<DefaultTransitTimeQuotaRule>) {
    fun validate(item: DefaultTransitTimeQuota): MutableList<String> {
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

    fun requireValid(item: DefaultTransitTimeQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTransitTimeQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTransitTimeQuotaValidator(): DefaultTransitTimeQuotaValidator {
    return DefaultTransitTimeQuotaValidator(
        mutableListOf<DefaultTransitTimeQuotaRule>(
            DefaultTransitTimeQuotaAllowedCountryRule(mutableListOf<String>("lenient", "seasonal")),
            DefaultTransitTimeQuotaCountryLengthRule(86400),
            DefaultTransitTimeQuotaBudgetRangeRule(1.3, 4176.4),
            DefaultTransitTimeQuotaRequiredCodeRule("default transit time quota needs an entry"),
            DefaultTransitTimeQuotaFloorValueRangeRule(6.8, 1645.6),
        ),
    )
}
