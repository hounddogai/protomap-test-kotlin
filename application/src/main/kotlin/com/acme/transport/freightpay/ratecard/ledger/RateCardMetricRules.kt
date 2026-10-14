package com.acme.transport.freightpay.ratecard.ledger

/** A rule a rate card metric must pass. Check returns its problems, or none. */
interface RateCardMetricRule {
    fun check(item: RateCardMetric): MutableList<String>
}

data class RateCardMetricCodeLengthRule(var maxLength: Int) : RateCardMetricRule {
    override fun check(item: RateCardMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardMetricPhoneLengthRule(var maxLength: Int) : RateCardMetricRule {
    override fun check(item: RateCardMetric): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardMetricCodePrefixRule(var prefix: String) : RateCardMetricRule {
    override fun check(item: RateCardMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card metric. */
data class RateCardMetricValidator(var rules: MutableList<RateCardMetricRule>) {
    fun validate(item: RateCardMetric): MutableList<String> {
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

    fun requireValid(item: RateCardMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardMetricValidator(): RateCardMetricValidator {
    return RateCardMetricValidator(
        mutableListOf<RateCardMetricRule>(
            RateCardMetricCodeLengthRule(120),
            RateCardMetricPhoneLengthRule(32),
            RateCardMetricCodePrefixRule("Q"),
        ),
    )
}
