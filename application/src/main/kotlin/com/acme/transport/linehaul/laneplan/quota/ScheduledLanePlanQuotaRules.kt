package com.acme.transport.linehaul.laneplan.quota

/** A rule a scheduled lane plan quota must pass. Check returns its problems, or none. */
interface ScheduledLanePlanQuotaRule {
    fun check(item: ScheduledLanePlanQuota): MutableList<String>
}

data class ScheduledLanePlanQuotaCodeLengthRule(var maxLength: Int) : ScheduledLanePlanQuotaRule {
    override fun check(item: ScheduledLanePlanQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanQuotaRequiredLabelRule(var message: String) : ScheduledLanePlanQuotaRule {
    override fun check(item: ScheduledLanePlanQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanQuotaDescriptionLengthRule(var maxLength: Int) : ScheduledLanePlanQuotaRule {
    override fun check(item: ScheduledLanePlanQuota): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanQuotaLabelLengthRule(var maxLength: Int) : ScheduledLanePlanQuotaRule {
    override fun check(item: ScheduledLanePlanQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanQuotaCodePrefixRule(var prefix: String) : ScheduledLanePlanQuotaRule {
    override fun check(item: ScheduledLanePlanQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanQuotaRevisionRangeRule(var min: Int, var max: Int) : ScheduledLanePlanQuotaRule {
    override fun check(item: ScheduledLanePlanQuota): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled lane plan quota. */
data class ScheduledLanePlanQuotaValidator(var rules: MutableList<ScheduledLanePlanQuotaRule>) {
    fun validate(item: ScheduledLanePlanQuota): MutableList<String> {
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

    fun requireValid(item: ScheduledLanePlanQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLanePlanQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLanePlanQuotaValidator(): ScheduledLanePlanQuotaValidator {
    return ScheduledLanePlanQuotaValidator(
        mutableListOf<ScheduledLanePlanQuotaRule>(
            ScheduledLanePlanQuotaCodeLengthRule(120),
            ScheduledLanePlanQuotaRequiredLabelRule("scheduled lane plan quota needs a value"),
            ScheduledLanePlanQuotaDescriptionLengthRule(86400),
            ScheduledLanePlanQuotaLabelLengthRule(16),
            ScheduledLanePlanQuotaCodePrefixRule("Q"),
            ScheduledLanePlanQuotaRevisionRangeRule(255, 1000),
        ),
    )
}
