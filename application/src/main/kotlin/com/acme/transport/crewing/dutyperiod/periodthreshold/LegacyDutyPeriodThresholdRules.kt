package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.text.normalize

/** A rule a legacy duty period threshold must pass. Check returns its problems, or none. */
interface LegacyDutyPeriodThresholdRule {
    fun check(item: LegacyDutyPeriodThreshold): MutableList<String>
}

data class LegacyDutyPeriodThresholdAllowedCodeRule(
    var allowed: MutableList<String>,
) : LegacyDutyPeriodThresholdRule {
    override fun check(item: LegacyDutyPeriodThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDutyPeriodThresholdCodeLengthRule(var maxLength: Int) : LegacyDutyPeriodThresholdRule {
    override fun check(item: LegacyDutyPeriodThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDutyPeriodThresholdAmountRangeRule(
    var min: Double,
    var max: Double,
) : LegacyDutyPeriodThresholdRule {
    override fun check(item: LegacyDutyPeriodThreshold): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDutyPeriodThresholdEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : LegacyDutyPeriodThresholdRule {
    override fun check(item: LegacyDutyPeriodThreshold): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy duty period threshold. */
data class LegacyDutyPeriodThresholdValidator(var rules: MutableList<LegacyDutyPeriodThresholdRule>) {
    fun validate(item: LegacyDutyPeriodThreshold): MutableList<String> {
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

    fun requireValid(item: LegacyDutyPeriodThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDutyPeriodThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDutyPeriodThresholdValidator(): LegacyDutyPeriodThresholdValidator {
    return LegacyDutyPeriodThresholdValidator(
        mutableListOf<LegacyDutyPeriodThresholdRule>(
            LegacyDutyPeriodThresholdAllowedCodeRule(mutableListOf<String>("manual", "lenient", "global")),
            LegacyDutyPeriodThresholdCodeLengthRule(86400),
            LegacyDutyPeriodThresholdAmountRangeRule(0.7, 2086.7),
            LegacyDutyPeriodThresholdEndsAtRangeRule(80, 3600),
        ),
    )
}
