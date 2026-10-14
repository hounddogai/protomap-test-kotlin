package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.text.normalize

/** A rule a legacy coaching quota must pass. Check returns its problems, or none. */
interface LegacyCoachingQuotaRule {
    fun check(item: LegacyCoachingQuota): MutableList<String>
}

data class LegacyCoachingQuotaCodePrefixRule(var prefix: String) : LegacyCoachingQuotaRule {
    override fun check(item: LegacyCoachingQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingQuotaAllowedRegionRule(var allowed: MutableList<String>) : LegacyCoachingQuotaRule {
    override fun check(item: LegacyCoachingQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is expired")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingQuotaCodeLengthRule(var maxLength: Int) : LegacyCoachingQuotaRule {
    override fun check(item: LegacyCoachingQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingQuotaRegionLengthRule(var maxLength: Int) : LegacyCoachingQuotaRule {
    override fun check(item: LegacyCoachingQuota): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingQuotaVersionRangeRule(var min: Int, var max: Int) : LegacyCoachingQuotaRule {
    override fun check(item: LegacyCoachingQuota): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingQuotaLabelLengthRule(var maxLength: Int) : LegacyCoachingQuotaRule {
    override fun check(item: LegacyCoachingQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy coaching quota. */
data class LegacyCoachingQuotaValidator(var rules: MutableList<LegacyCoachingQuotaRule>) {
    fun validate(item: LegacyCoachingQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCoachingQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCoachingQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCoachingQuotaValidator(): LegacyCoachingQuotaValidator {
    return LegacyCoachingQuotaValidator(
        mutableListOf<LegacyCoachingQuotaRule>(
            LegacyCoachingQuotaCodePrefixRule("Q"),
            LegacyCoachingQuotaAllowedRegionRule(mutableListOf<String>()),
            LegacyCoachingQuotaCodeLengthRule(32),
            LegacyCoachingQuotaRegionLengthRule(255),
            LegacyCoachingQuotaVersionRangeRule(32, 80),
            LegacyCoachingQuotaLabelLengthRule(64),
        ),
    )
}
