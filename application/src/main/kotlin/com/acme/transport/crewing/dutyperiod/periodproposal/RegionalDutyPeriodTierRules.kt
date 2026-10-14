package com.acme.transport.crewing.dutyperiod.periodproposal

/** A rule a regional duty period tier must pass. Check returns its problems, or none. */
interface RegionalDutyPeriodTierRule {
    fun check(item: RegionalDutyPeriodTier): MutableList<String>
}

data class RegionalDutyPeriodTierTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RegionalDutyPeriodTierRule {
    override fun check(item: RegionalDutyPeriodTier): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RegionalDutyPeriodTierShiftCodeLengthRule(var maxLength: Int) : RegionalDutyPeriodTierRule {
    override fun check(item: RegionalDutyPeriodTier): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodTierActiveGuardRule(var limit: Int) : RegionalDutyPeriodTierRule {
    override fun check(item: RegionalDutyPeriodTier): MutableList<String> {
        if (item.active && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDutyPeriodTierCodePrefixRule(var prefix: String) : RegionalDutyPeriodTierRule {
    override fun check(item: RegionalDutyPeriodTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional duty period tier. */
data class RegionalDutyPeriodTierValidator(var rules: MutableList<RegionalDutyPeriodTierRule>) {
    fun validate(item: RegionalDutyPeriodTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDutyPeriodTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDutyPeriodTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDutyPeriodTierValidator(): RegionalDutyPeriodTierValidator {
    return RegionalDutyPeriodTierValidator(
        mutableListOf<RegionalDutyPeriodTierRule>(
            RegionalDutyPeriodTierTagsLimitRule(32, mutableListOf<String>("lenient", "tiered", "primary")),
            RegionalDutyPeriodTierShiftCodeLengthRule(64),
            RegionalDutyPeriodTierActiveGuardRule(255),
            RegionalDutyPeriodTierCodePrefixRule("OPS"),
        ),
    )
}
