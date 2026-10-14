package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.text.normalize

/** A rule a weighted scorecard quota must pass. Check returns its problems, or none. */
interface WeightedScorecardQuotaRule {
    fun check(item: WeightedScorecardQuota): MutableList<String>
}

data class WeightedScorecardQuotaCodeLengthRule(var maxLength: Int) : WeightedScorecardQuotaRule {
    override fun check(item: WeightedScorecardQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedScorecardQuotaDiscountRangeRule(
    var min: Double,
    var max: Double,
) : WeightedScorecardQuotaRule {
    override fun check(item: WeightedScorecardQuota): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedScorecardQuotaAllowedDepotRule(
    var allowed: MutableList<String>,
) : WeightedScorecardQuotaRule {
    override fun check(item: WeightedScorecardQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class WeightedScorecardQuotaEtaMinutesRangeRule(var min: Int, var max: Int) : WeightedScorecardQuotaRule {
    override fun check(item: WeightedScorecardQuota): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedScorecardQuotaRequiredCodeRule(var message: String) : WeightedScorecardQuotaRule {
    override fun check(item: WeightedScorecardQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedScorecardQuotaMaximumRangeRule(
    var min: Double,
    var max: Double,
) : WeightedScorecardQuotaRule {
    override fun check(item: WeightedScorecardQuota): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted scorecard quota. */
data class WeightedScorecardQuotaValidator(var rules: MutableList<WeightedScorecardQuotaRule>) {
    fun validate(item: WeightedScorecardQuota): MutableList<String> {
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

    fun requireValid(item: WeightedScorecardQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedScorecardQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedScorecardQuotaValidator(): WeightedScorecardQuotaValidator {
    return WeightedScorecardQuotaValidator(
        mutableListOf<WeightedScorecardQuotaRule>(
            WeightedScorecardQuotaCodeLengthRule(80),
            WeightedScorecardQuotaDiscountRangeRule(4.3, 1480.6),
            WeightedScorecardQuotaAllowedDepotRule(mutableListOf<String>("scheduled", "adaptive", "tiered")),
            WeightedScorecardQuotaEtaMinutesRangeRule(86400, 86400),
            WeightedScorecardQuotaRequiredCodeRule("weighted scorecard quota needs an entry"),
            WeightedScorecardQuotaMaximumRangeRule(0.3, 3062.1),
        ),
    )
}
