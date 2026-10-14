package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.text.normalize

/** A rule a tiered duty period digest must pass. Check returns its problems, or none. */
interface TieredDutyPeriodDigestRule {
    fun check(item: TieredDutyPeriodDigest): MutableList<String>
}

data class TieredDutyPeriodDigestBudgetRangeRule(
    var min: Double,
    var max: Double,
) : TieredDutyPeriodDigestRule {
    override fun check(item: TieredDutyPeriodDigest): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class TieredDutyPeriodDigestCodePrefixRule(var prefix: String) : TieredDutyPeriodDigestRule {
    override fun check(item: TieredDutyPeriodDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredDutyPeriodDigestAllowedNameRule(
    var allowed: MutableList<String>,
) : TieredDutyPeriodDigestRule {
    override fun check(item: TieredDutyPeriodDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class TieredDutyPeriodDigestSegmentLengthRule(var maxLength: Int) : TieredDutyPeriodDigestRule {
    override fun check(item: TieredDutyPeriodDigest): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered duty period digest. */
data class TieredDutyPeriodDigestValidator(var rules: MutableList<TieredDutyPeriodDigestRule>) {
    fun validate(item: TieredDutyPeriodDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDutyPeriodDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDutyPeriodDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDutyPeriodDigestValidator(): TieredDutyPeriodDigestValidator {
    return TieredDutyPeriodDigestValidator(
        mutableListOf<TieredDutyPeriodDigestRule>(
            TieredDutyPeriodDigestBudgetRangeRule(0.8, 4025.1),
            TieredDutyPeriodDigestCodePrefixRule("Q"),
            TieredDutyPeriodDigestAllowedNameRule(mutableListOf<String>("strict", "seasonal", "capped")),
            TieredDutyPeriodDigestSegmentLengthRule(32),
        ),
    )
}
