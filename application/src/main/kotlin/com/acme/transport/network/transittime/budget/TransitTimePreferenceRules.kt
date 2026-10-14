package com.acme.transport.network.transittime.budget

/** A rule a transit time preference must pass. Check returns its problems, or none. */
interface TransitTimePreferenceRule {
    fun check(item: TransitTimePreference): MutableList<String>
}

data class TransitTimePreferenceRetriesRangeRule(var min: Int, var max: Int) : TransitTimePreferenceRule {
    override fun check(item: TransitTimePreference): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class TransitTimePreferenceRequiredCurrencyRule(var message: String) : TransitTimePreferenceRule {
    override fun check(item: TransitTimePreference): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimePreferenceCodePrefixRule(var prefix: String) : TransitTimePreferenceRule {
    override fun check(item: TransitTimePreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimePreferenceRequiredContactEmailRule(var message: String) : TransitTimePreferenceRule {
    override fun check(item: TransitTimePreference): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimePreferenceRequiredNameRule(var message: String) : TransitTimePreferenceRule {
    override fun check(item: TransitTimePreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time preference. */
data class TransitTimePreferenceValidator(var rules: MutableList<TransitTimePreferenceRule>) {
    fun validate(item: TransitTimePreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimePreferenceValidator(): TransitTimePreferenceValidator {
    return TransitTimePreferenceValidator(
        mutableListOf<TransitTimePreferenceRule>(
            TransitTimePreferenceRetriesRangeRule(80, 255),
            TransitTimePreferenceRequiredCurrencyRule("transit time preference needs this field"),
            TransitTimePreferenceCodePrefixRule("X"),
            TransitTimePreferenceRequiredContactEmailRule("transit time preference needs this field"),
            TransitTimePreferenceRequiredNameRule("transit time preference needs a value"),
        ),
    )
}
