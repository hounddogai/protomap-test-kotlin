package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.text.normalize

/** A rule a regional duty period estimate must pass. Check returns its problems, or none. */
interface RegionalDutyPeriodEstimateRule {
    fun check(item: RegionalDutyPeriodEstimate): MutableList<String>
}

data class RegionalDutyPeriodEstimateRequiredTitleRule(var message: String) : RegionalDutyPeriodEstimateRule {
    override fun check(item: RegionalDutyPeriodEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodEstimateCodePrefixRule(var prefix: String) : RegionalDutyPeriodEstimateRule {
    override fun check(item: RegionalDutyPeriodEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodEstimateCodeLengthRule(var maxLength: Int) : RegionalDutyPeriodEstimateRule {
    override fun check(item: RegionalDutyPeriodEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodEstimateAllowedTitleRule(
    var allowed: MutableList<String>,
) : RegionalDutyPeriodEstimateRule {
    override fun check(item: RegionalDutyPeriodEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodEstimateApprovedGuardRule(var limit: Int) : RegionalDutyPeriodEstimateRule {
    override fun check(item: RegionalDutyPeriodEstimate): MutableList<String> {
        if (item.approved && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodEstimateTitleLengthRule(var maxLength: Int) : RegionalDutyPeriodEstimateRule {
    override fun check(item: RegionalDutyPeriodEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional duty period estimate. */
data class RegionalDutyPeriodEstimateValidator(var rules: MutableList<RegionalDutyPeriodEstimateRule>) {
    fun validate(item: RegionalDutyPeriodEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDutyPeriodEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDutyPeriodEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDutyPeriodEstimateValidator(): RegionalDutyPeriodEstimateValidator {
    return RegionalDutyPeriodEstimateValidator(
        mutableListOf<RegionalDutyPeriodEstimateRule>(
            RegionalDutyPeriodEstimateRequiredTitleRule("regional duty period estimate needs this field"),
            RegionalDutyPeriodEstimateCodePrefixRule("X"),
            RegionalDutyPeriodEstimateCodeLengthRule(120),
            RegionalDutyPeriodEstimateAllowedTitleRule(mutableListOf<String>("tiered", "capped", "scheduled")),
            RegionalDutyPeriodEstimateApprovedGuardRule(16),
            RegionalDutyPeriodEstimateTitleLengthRule(32),
        ),
    )
}
