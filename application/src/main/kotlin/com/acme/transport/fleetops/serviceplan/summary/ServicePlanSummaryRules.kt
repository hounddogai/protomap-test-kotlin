package com.acme.transport.fleetops.serviceplan.summary

/** A rule a service plan summary must pass. Check returns its problems, or none. */
interface ServicePlanSummaryRule {
    fun check(item: ServicePlanSummary): MutableList<String>
}

data class ServicePlanSummaryPhoneLengthRule(var maxLength: Int) : ServicePlanSummaryRule {
    override fun check(item: ServicePlanSummary): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSummaryCeilingRangeRule(var min: Double, var max: Double) : ServicePlanSummaryRule {
    override fun check(item: ServicePlanSummary): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class ServicePlanSummaryPinnedGuardRule(var limit: Double) : ServicePlanSummaryRule {
    override fun check(item: ServicePlanSummary): MutableList<String> {
        if (item.pinned && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSummaryCodeLengthRule(var maxLength: Int) : ServicePlanSummaryRule {
    override fun check(item: ServicePlanSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSummaryRequiredCodeRule(var message: String) : ServicePlanSummaryRule {
    override fun check(item: ServicePlanSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanSummaryCodePrefixRule(var prefix: String) : ServicePlanSummaryRule {
    override fun check(item: ServicePlanSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan summary. */
data class ServicePlanSummaryValidator(var rules: MutableList<ServicePlanSummaryRule>) {
    fun validate(item: ServicePlanSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanSummaryValidator(): ServicePlanSummaryValidator {
    return ServicePlanSummaryValidator(
        mutableListOf<ServicePlanSummaryRule>(
            ServicePlanSummaryPhoneLengthRule(32),
            ServicePlanSummaryCeilingRangeRule(1.0, 4577.5),
            ServicePlanSummaryPinnedGuardRule(0.3),
            ServicePlanSummaryCodeLengthRule(255),
            ServicePlanSummaryRequiredCodeRule("service plan summary needs an entry"),
            ServicePlanSummaryCodePrefixRule("X"),
        ),
    )
}
