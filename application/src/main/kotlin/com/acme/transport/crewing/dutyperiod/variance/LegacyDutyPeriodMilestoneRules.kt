package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.text.normalize

/** A rule a legacy duty period milestone must pass. Check returns its problems, or none. */
interface LegacyDutyPeriodMilestoneRule {
    fun check(item: LegacyDutyPeriodMilestone): MutableList<String>
}

data class LegacyDutyPeriodMilestoneTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LegacyDutyPeriodMilestoneRule {
    override fun check(item: LegacyDutyPeriodMilestone): MutableList<String> {
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

data class LegacyDutyPeriodMilestoneRequiredLabelRule(var message: String) : LegacyDutyPeriodMilestoneRule {
    override fun check(item: LegacyDutyPeriodMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDutyPeriodMilestoneAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : LegacyDutyPeriodMilestoneRule {
    override fun check(item: LegacyDutyPeriodMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDutyPeriodMilestoneChecksumLengthRule(var maxLength: Int) : LegacyDutyPeriodMilestoneRule {
    override fun check(item: LegacyDutyPeriodMilestone): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy duty period milestone. */
data class LegacyDutyPeriodMilestoneValidator(var rules: MutableList<LegacyDutyPeriodMilestoneRule>) {
    fun validate(item: LegacyDutyPeriodMilestone): MutableList<String> {
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

    fun requireValid(item: LegacyDutyPeriodMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDutyPeriodMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDutyPeriodMilestoneValidator(): LegacyDutyPeriodMilestoneValidator {
    return LegacyDutyPeriodMilestoneValidator(
        mutableListOf<LegacyDutyPeriodMilestoneRule>(
            LegacyDutyPeriodMilestoneTagsLimitRule(255, mutableListOf<String>("legacy", "adaptive")),
            LegacyDutyPeriodMilestoneRequiredLabelRule("legacy duty period milestone needs a value"),
            LegacyDutyPeriodMilestoneAllowedTimezoneRule(mutableListOf<String>("lenient", "fallback")),
            LegacyDutyPeriodMilestoneChecksumLengthRule(1000),
        ),
    )
}
