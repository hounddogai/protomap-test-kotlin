package com.acme.transport.freightpay.dispute.disputebundle

/** A rule a scheduled dispute bundle must pass. Check returns its problems, or none. */
interface ScheduledDisputeBundleRule {
    fun check(item: ScheduledDisputeBundle): MutableList<String>
}

data class ScheduledDisputeBundleRequiredTimezoneRule(var message: String) : ScheduledDisputeBundleRule {
    override fun check(item: ScheduledDisputeBundle): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDisputeBundleRequiredCodeRule(var message: String) : ScheduledDisputeBundleRule {
    override fun check(item: ScheduledDisputeBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDisputeBundleRequiredCategoryRule(var message: String) : ScheduledDisputeBundleRule {
    override fun check(item: ScheduledDisputeBundle): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dispute bundle. */
data class ScheduledDisputeBundleValidator(var rules: MutableList<ScheduledDisputeBundleRule>) {
    fun validate(item: ScheduledDisputeBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDisputeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDisputeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDisputeBundleValidator(): ScheduledDisputeBundleValidator {
    return ScheduledDisputeBundleValidator(
        mutableListOf<ScheduledDisputeBundleRule>(
            ScheduledDisputeBundleRequiredTimezoneRule("scheduled dispute bundle needs this field"),
            ScheduledDisputeBundleRequiredCodeRule("scheduled dispute bundle needs a value"),
            ScheduledDisputeBundleRequiredCategoryRule("scheduled dispute bundle needs an entry"),
        ),
    )
}
