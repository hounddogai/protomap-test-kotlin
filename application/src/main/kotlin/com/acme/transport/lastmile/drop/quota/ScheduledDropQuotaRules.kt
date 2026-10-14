package com.acme.transport.lastmile.drop.quota

/** A rule a scheduled drop quota must pass. Check returns its problems, or none. */
interface ScheduledDropQuotaRule {
    fun check(item: ScheduledDropQuota): MutableList<String>
}

data class ScheduledDropQuotaRequiredTitleRule(var message: String) : ScheduledDropQuotaRule {
    override fun check(item: ScheduledDropQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDropQuotaLabelLengthRule(var maxLength: Int) : ScheduledDropQuotaRule {
    override fun check(item: ScheduledDropQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDropQuotaRegionLengthRule(var maxLength: Int) : ScheduledDropQuotaRule {
    override fun check(item: ScheduledDropQuota): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled drop quota. */
data class ScheduledDropQuotaValidator(var rules: MutableList<ScheduledDropQuotaRule>) {
    fun validate(item: ScheduledDropQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDropQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDropQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDropQuotaValidator(): ScheduledDropQuotaValidator {
    return ScheduledDropQuotaValidator(
        mutableListOf<ScheduledDropQuotaRule>(
            ScheduledDropQuotaRequiredTitleRule("scheduled drop quota needs a value"),
            ScheduledDropQuotaLabelLengthRule(64),
            ScheduledDropQuotaRegionLengthRule(64),
        ),
    )
}
