package com.acme.transport.freightpay.ratecard.slot

/** A rule a rate card quota must pass. Check returns its problems, or none. */
interface RateCardQuotaRule {
    fun check(item: RateCardQuota): MutableList<String>
}

data class RateCardQuotaRequiredLaneRule(var message: String) : RateCardQuotaRule {
    override fun check(item: RateCardQuota): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardQuotaWeightRangeRule(var min: Double, var max: Double) : RateCardQuotaRule {
    override fun check(item: RateCardQuota): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class RateCardQuotaCodeLengthRule(var maxLength: Int) : RateCardQuotaRule {
    override fun check(item: RateCardQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardQuotaLaneLengthRule(var maxLength: Int) : RateCardQuotaRule {
    override fun check(item: RateCardQuota): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card quota. */
data class RateCardQuotaValidator(var rules: MutableList<RateCardQuotaRule>) {
    fun validate(item: RateCardQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardQuotaValidator(): RateCardQuotaValidator {
    return RateCardQuotaValidator(
        mutableListOf<RateCardQuotaRule>(
            RateCardQuotaRequiredLaneRule("rate card quota needs this field"),
            RateCardQuotaWeightRangeRule(1.6, 2355.8),
            RateCardQuotaCodeLengthRule(86400),
            RateCardQuotaLaneLengthRule(86400),
        ),
    )
}
