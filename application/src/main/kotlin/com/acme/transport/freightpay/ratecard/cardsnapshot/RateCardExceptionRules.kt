package com.acme.transport.freightpay.ratecard.cardsnapshot

/** A rule a rate card exception must pass. Check returns its problems, or none. */
interface RateCardExceptionRule {
    fun check(item: RateCardException): MutableList<String>
}

data class RateCardExceptionRetriesRangeRule(var min: Int, var max: Int) : RateCardExceptionRule {
    override fun check(item: RateCardException): MutableList<String> {
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

data class RateCardExceptionRequiredRegionRule(var message: String) : RateCardExceptionRule {
    override fun check(item: RateCardException): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardExceptionCategoryLengthRule(var maxLength: Int) : RateCardExceptionRule {
    override fun check(item: RateCardException): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card exception. */
data class RateCardExceptionValidator(var rules: MutableList<RateCardExceptionRule>) {
    fun validate(item: RateCardException): MutableList<String> {
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

    fun requireValid(item: RateCardException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardException>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardExceptionValidator(): RateCardExceptionValidator {
    return RateCardExceptionValidator(
        mutableListOf<RateCardExceptionRule>(
            RateCardExceptionRetriesRangeRule(80, 255),
            RateCardExceptionRequiredRegionRule("rate card exception needs an entry"),
            RateCardExceptionCategoryLengthRule(120),
        ),
    )
}
