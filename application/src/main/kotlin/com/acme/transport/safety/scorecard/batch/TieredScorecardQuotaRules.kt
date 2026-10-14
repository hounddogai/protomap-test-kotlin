package com.acme.transport.safety.scorecard.batch

import com.acme.transport.kit.text.normalize

/** A rule a tiered scorecard quota must pass. Check returns its problems, or none. */
interface TieredScorecardQuotaRule {
    fun check(item: TieredScorecardQuota): MutableList<String>
}

data class TieredScorecardQuotaSourceLengthRule(var maxLength: Int) : TieredScorecardQuotaRule {
    override fun check(item: TieredScorecardQuota): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardQuotaAllowedNameRule(var allowed: MutableList<String>) : TieredScorecardQuotaRule {
    override fun check(item: TieredScorecardQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardQuotaRequiredContactEmailRule(var message: String) : TieredScorecardQuotaRule {
    override fun check(item: TieredScorecardQuota): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardQuotaDiscountRangeRule(var min: Double, var max: Double) : TieredScorecardQuotaRule {
    override fun check(item: TieredScorecardQuota): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered scorecard quota. */
data class TieredScorecardQuotaValidator(var rules: MutableList<TieredScorecardQuotaRule>) {
    fun validate(item: TieredScorecardQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredScorecardQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredScorecardQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredScorecardQuotaValidator(): TieredScorecardQuotaValidator {
    return TieredScorecardQuotaValidator(
        mutableListOf<TieredScorecardQuotaRule>(
            TieredScorecardQuotaSourceLengthRule(120),
            TieredScorecardQuotaAllowedNameRule(mutableListOf<String>("rolling", "fallback")),
            TieredScorecardQuotaRequiredContactEmailRule("tiered scorecard quota needs this field"),
            TieredScorecardQuotaDiscountRangeRule(9.0, 1648.1),
        ),
    )
}
