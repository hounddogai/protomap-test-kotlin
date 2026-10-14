package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.text.normalize

/** A rule a manual service plan request must pass. Check returns its problems, or none. */
interface ManualServicePlanRequestRule {
    fun check(item: ManualServicePlanRequest): MutableList<String>
}

data class ManualServicePlanRequestExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualServicePlanRequestRule {
    override fun check(item: ManualServicePlanRequest): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class ManualServicePlanRequestRequiredTitleRule(var message: String) : ManualServicePlanRequestRule {
    override fun check(item: ManualServicePlanRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanRequestAllowedTitleRule(
    var allowed: MutableList<String>,
) : ManualServicePlanRequestRule {
    override fun check(item: ManualServicePlanRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanRequestRequiredSummaryRule(var message: String) : ManualServicePlanRequestRule {
    override fun check(item: ManualServicePlanRequest): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanRequestRegionLengthRule(var maxLength: Int) : ManualServicePlanRequestRule {
    override fun check(item: ManualServicePlanRequest): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanRequestRequiredRegionRule(var message: String) : ManualServicePlanRequestRule {
    override fun check(item: ManualServicePlanRequest): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual service plan request. */
data class ManualServicePlanRequestValidator(var rules: MutableList<ManualServicePlanRequestRule>) {
    fun validate(item: ManualServicePlanRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualServicePlanRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualServicePlanRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualServicePlanRequestValidator(): ManualServicePlanRequestValidator {
    return ManualServicePlanRequestValidator(
        mutableListOf<ManualServicePlanRequestRule>(
            ManualServicePlanRequestExpiresAtRangeRule(64, 80),
            ManualServicePlanRequestRequiredTitleRule("manual service plan request needs a value"),
            ManualServicePlanRequestAllowedTitleRule(mutableListOf<String>()),
            ManualServicePlanRequestRequiredSummaryRule("manual service plan request needs this field"),
            ManualServicePlanRequestRegionLengthRule(80),
            ManualServicePlanRequestRequiredRegionRule("manual service plan request needs a value"),
        ),
    )
}
