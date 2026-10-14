package com.acme.transport.freightpay.ratecard.adjustment

/** A rule a rate card override must pass. Check returns its problems, or none. */
interface RateCardOverrideRule {
    fun check(item: RateCardOverride): MutableList<String>
}

data class RateCardOverrideRequiredReviewerRule(var message: String) : RateCardOverrideRule {
    override fun check(item: RateCardOverride): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardOverrideCodePrefixRule(var prefix: String) : RateCardOverrideRule {
    override fun check(item: RateCardOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardOverrideRequiredShiftCodeRule(var message: String) : RateCardOverrideRule {
    override fun check(item: RateCardOverride): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card override. */
data class RateCardOverrideValidator(var rules: MutableList<RateCardOverrideRule>) {
    fun validate(item: RateCardOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardOverrideValidator(): RateCardOverrideValidator {
    return RateCardOverrideValidator(
        mutableListOf<RateCardOverrideRule>(
            RateCardOverrideRequiredReviewerRule("rate card override needs this field"),
            RateCardOverrideCodePrefixRule("X"),
            RateCardOverrideRequiredShiftCodeRule("rate card override needs an entry"),
        ),
    )
}
