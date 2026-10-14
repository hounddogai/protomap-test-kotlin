package com.acme.transport.crewing.roster.rosterquota

/** A rule a scheduled roster quota must pass. Check returns its problems, or none. */
interface ScheduledRosterQuotaRule {
    fun check(item: ScheduledRosterQuota): MutableList<String>
}

data class ScheduledRosterQuotaRequiredTitleRule(var message: String) : ScheduledRosterQuotaRule {
    override fun check(item: ScheduledRosterQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterQuotaRatioRangeRule(var min: Double, var max: Double) : ScheduledRosterQuotaRule {
    override fun check(item: ScheduledRosterQuota): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class ScheduledRosterQuotaRequiredDriverIdRule(var message: String) : ScheduledRosterQuotaRule {
    override fun check(item: ScheduledRosterQuota): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRosterQuotaTitleLengthRule(var maxLength: Int) : ScheduledRosterQuotaRule {
    override fun check(item: ScheduledRosterQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled roster quota. */
data class ScheduledRosterQuotaValidator(var rules: MutableList<ScheduledRosterQuotaRule>) {
    fun validate(item: ScheduledRosterQuota): MutableList<String> {
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

    fun requireValid(item: ScheduledRosterQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRosterQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRosterQuotaValidator(): ScheduledRosterQuotaValidator {
    return ScheduledRosterQuotaValidator(
        mutableListOf<ScheduledRosterQuotaRule>(
            ScheduledRosterQuotaRequiredTitleRule("scheduled roster quota needs this field"),
            ScheduledRosterQuotaRatioRangeRule(1.0, 1708.4),
            ScheduledRosterQuotaRequiredDriverIdRule("scheduled roster quota needs this field"),
            ScheduledRosterQuotaTitleLengthRule(16),
        ),
    )
}
