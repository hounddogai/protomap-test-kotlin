package com.acme.transport.freightpay.audit.tier

/** A rule a scheduled audit bundle must pass. Check returns its problems, or none. */
interface ScheduledAuditBundleRule {
    fun check(item: ScheduledAuditBundle): MutableList<String>
}

data class ScheduledAuditBundleRequiredDockRule(var message: String) : ScheduledAuditBundleRule {
    override fun check(item: ScheduledAuditBundle): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAuditBundleGraceDaysRangeRule(var min: Int, var max: Int) : ScheduledAuditBundleRule {
    override fun check(item: ScheduledAuditBundle): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class ScheduledAuditBundleRequiredCodeRule(var message: String) : ScheduledAuditBundleRule {
    override fun check(item: ScheduledAuditBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAuditBundleOdometerRangeRule(var min: Int, var max: Int) : ScheduledAuditBundleRule {
    override fun check(item: ScheduledAuditBundle): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class ScheduledAuditBundleCodePrefixRule(var prefix: String) : ScheduledAuditBundleRule {
    override fun check(item: ScheduledAuditBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAuditBundleNameLengthRule(var maxLength: Int) : ScheduledAuditBundleRule {
    override fun check(item: ScheduledAuditBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled audit bundle. */
data class ScheduledAuditBundleValidator(var rules: MutableList<ScheduledAuditBundleRule>) {
    fun validate(item: ScheduledAuditBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAuditBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAuditBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAuditBundleValidator(): ScheduledAuditBundleValidator {
    return ScheduledAuditBundleValidator(
        mutableListOf<ScheduledAuditBundleRule>(
            ScheduledAuditBundleRequiredDockRule("scheduled audit bundle needs this field"),
            ScheduledAuditBundleGraceDaysRangeRule(16, 32),
            ScheduledAuditBundleRequiredCodeRule("scheduled audit bundle needs a value"),
            ScheduledAuditBundleOdometerRangeRule(120, 86400),
            ScheduledAuditBundleCodePrefixRule("Q"),
            ScheduledAuditBundleNameLengthRule(120),
        ),
    )
}
