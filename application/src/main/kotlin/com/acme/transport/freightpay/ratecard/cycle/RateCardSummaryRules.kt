package com.acme.transport.freightpay.ratecard.cycle

/** A rule a rate card summary must pass. Check returns its problems, or none. */
interface RateCardSummaryRule {
    fun check(item: RateCardSummary): MutableList<String>
}

data class RateCardSummaryLocaleLengthRule(var maxLength: Int) : RateCardSummaryRule {
    override fun check(item: RateCardSummary): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardSummaryRequiredLocaleRule(var message: String) : RateCardSummaryRule {
    override fun check(item: RateCardSummary): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardSummaryCodePrefixRule(var prefix: String) : RateCardSummaryRule {
    override fun check(item: RateCardSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card summary. */
data class RateCardSummaryValidator(var rules: MutableList<RateCardSummaryRule>) {
    fun validate(item: RateCardSummary): MutableList<String> {
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

    fun requireValid(item: RateCardSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardSummaryValidator(): RateCardSummaryValidator {
    return RateCardSummaryValidator(
        mutableListOf<RateCardSummaryRule>(
            RateCardSummaryLocaleLengthRule(16),
            RateCardSummaryRequiredLocaleRule("rate card summary needs an entry"),
            RateCardSummaryCodePrefixRule("OPS"),
        ),
    )
}
