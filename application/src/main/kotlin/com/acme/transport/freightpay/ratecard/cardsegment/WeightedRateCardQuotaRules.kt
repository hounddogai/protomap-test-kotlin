package com.acme.transport.freightpay.ratecard.cardsegment

/** A rule a weighted rate card quota must pass. Check returns its problems, or none. */
interface WeightedRateCardQuotaRule {
    fun check(item: WeightedRateCardQuota): MutableList<String>
}

data class WeightedRateCardQuotaSourceLengthRule(var maxLength: Int) : WeightedRateCardQuotaRule {
    override fun check(item: WeightedRateCardQuota): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardQuotaCapacityRangeRule(var min: Int, var max: Int) : WeightedRateCardQuotaRule {
    override fun check(item: WeightedRateCardQuota): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardQuotaAttemptsRangeRule(var min: Int, var max: Int) : WeightedRateCardQuotaRule {
    override fun check(item: WeightedRateCardQuota): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted rate card quota. */
data class WeightedRateCardQuotaValidator(var rules: MutableList<WeightedRateCardQuotaRule>) {
    fun validate(item: WeightedRateCardQuota): MutableList<String> {
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

    fun requireValid(item: WeightedRateCardQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRateCardQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRateCardQuotaValidator(): WeightedRateCardQuotaValidator {
    return WeightedRateCardQuotaValidator(
        mutableListOf<WeightedRateCardQuotaRule>(
            WeightedRateCardQuotaSourceLengthRule(16),
            WeightedRateCardQuotaCapacityRangeRule(64, 86400),
            WeightedRateCardQuotaAttemptsRangeRule(16, 64),
        ),
    )
}
