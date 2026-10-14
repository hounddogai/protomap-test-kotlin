package com.acme.transport.freightpay.ratecard.entry

/** A rule a scheduled rate card baseline must pass. Check returns its problems, or none. */
interface ScheduledRateCardBaselineRule {
    fun check(item: ScheduledRateCardBaseline): MutableList<String>
}

data class ScheduledRateCardBaselineRateRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledRateCardBaselineRule {
    override fun check(item: ScheduledRateCardBaseline): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRateCardBaselineRequiredCountryRule(var message: String) : ScheduledRateCardBaselineRule {
    override fun check(item: ScheduledRateCardBaseline): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRateCardBaselineCodePrefixRule(var prefix: String) : ScheduledRateCardBaselineRule {
    override fun check(item: ScheduledRateCardBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRateCardBaselineRequiredExternalRefRule(
    var message: String,
) : ScheduledRateCardBaselineRule {
    override fun check(item: ScheduledRateCardBaseline): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled rate card baseline. */
data class ScheduledRateCardBaselineValidator(var rules: MutableList<ScheduledRateCardBaselineRule>) {
    fun validate(item: ScheduledRateCardBaseline): MutableList<String> {
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

    fun requireValid(item: ScheduledRateCardBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRateCardBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRateCardBaselineValidator(): ScheduledRateCardBaselineValidator {
    return ScheduledRateCardBaselineValidator(
        mutableListOf<ScheduledRateCardBaselineRule>(
            ScheduledRateCardBaselineRateRangeRule(2.6, 3706.2),
            ScheduledRateCardBaselineRequiredCountryRule("scheduled rate card baseline needs a value"),
            ScheduledRateCardBaselineCodePrefixRule("X"),
            ScheduledRateCardBaselineRequiredExternalRefRule("scheduled rate card baseline needs this field"),
        ),
    )
}
