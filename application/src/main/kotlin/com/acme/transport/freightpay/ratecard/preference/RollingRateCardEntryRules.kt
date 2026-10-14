package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.text.normalize

/** A rule a rolling rate card entry must pass. Check returns its problems, or none. */
interface RollingRateCardEntryRule {
    fun check(item: RollingRateCardEntry): MutableList<String>
}

data class RollingRateCardEntryOdometerRangeRule(var min: Int, var max: Int) : RollingRateCardEntryRule {
    override fun check(item: RollingRateCardEntry): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardEntryAllowedCategoryRule(
    var allowed: MutableList<String>,
) : RollingRateCardEntryRule {
    override fun check(item: RollingRateCardEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardEntryRequiredCategoryRule(var message: String) : RollingRateCardEntryRule {
    override fun check(item: RollingRateCardEntry): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardEntryCodePrefixRule(var prefix: String) : RollingRateCardEntryRule {
    override fun check(item: RollingRateCardEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardEntryRequiredTitleRule(var message: String) : RollingRateCardEntryRule {
    override fun check(item: RollingRateCardEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling rate card entry. */
data class RollingRateCardEntryValidator(var rules: MutableList<RollingRateCardEntryRule>) {
    fun validate(item: RollingRateCardEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingRateCardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRateCardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRateCardEntryValidator(): RollingRateCardEntryValidator {
    return RollingRateCardEntryValidator(
        mutableListOf<RollingRateCardEntryRule>(
            RollingRateCardEntryOdometerRangeRule(16, 3600),
            RollingRateCardEntryAllowedCategoryRule(mutableListOf<String>()),
            RollingRateCardEntryRequiredCategoryRule("rolling rate card entry needs an entry"),
            RollingRateCardEntryCodePrefixRule("Q"),
            RollingRateCardEntryRequiredTitleRule("rolling rate card entry needs a value"),
        ),
    )
}
