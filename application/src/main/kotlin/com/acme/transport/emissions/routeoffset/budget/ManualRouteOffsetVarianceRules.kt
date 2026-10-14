package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.text.normalize

/** A rule a manual route offset variance must pass. Check returns its problems, or none. */
interface ManualRouteOffsetVarianceRule {
    fun check(item: ManualRouteOffsetVariance): MutableList<String>
}

data class ManualRouteOffsetVarianceAllowedLabelRule(
    var allowed: MutableList<String>,
) : ManualRouteOffsetVarianceRule {
    override fun check(item: ManualRouteOffsetVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ManualRouteOffsetVarianceArchivedGuardRule(var limit: Double) : ManualRouteOffsetVarianceRule {
    override fun check(item: ManualRouteOffsetVariance): MutableList<String> {
        if (item.archived && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ManualRouteOffsetVarianceRequiredCodeRule(var message: String) : ManualRouteOffsetVarianceRule {
    override fun check(item: ManualRouteOffsetVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRouteOffsetVarianceRetriesRangeRule(
    var min: Int,
    var max: Int,
) : ManualRouteOffsetVarianceRule {
    override fun check(item: ManualRouteOffsetVariance): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualRouteOffsetVarianceCodePrefixRule(var prefix: String) : ManualRouteOffsetVarianceRule {
    override fun check(item: ManualRouteOffsetVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualRouteOffsetVarianceRatioRangeRule(
    var min: Double,
    var max: Double,
) : ManualRouteOffsetVarianceRule {
    override fun check(item: ManualRouteOffsetVariance): MutableList<String> {
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

/** Runs every rule of a manual route offset variance. */
data class ManualRouteOffsetVarianceValidator(var rules: MutableList<ManualRouteOffsetVarianceRule>) {
    fun validate(item: ManualRouteOffsetVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRouteOffsetVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRouteOffsetVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRouteOffsetVarianceValidator(): ManualRouteOffsetVarianceValidator {
    return ManualRouteOffsetVarianceValidator(
        mutableListOf<ManualRouteOffsetVarianceRule>(
            ManualRouteOffsetVarianceAllowedLabelRule(mutableListOf<String>("legacy", "default", "default")),
            ManualRouteOffsetVarianceArchivedGuardRule(8.2),
            ManualRouteOffsetVarianceRequiredCodeRule("manual route offset variance needs an entry"),
            ManualRouteOffsetVarianceRetriesRangeRule(80, 3600),
            ManualRouteOffsetVarianceCodePrefixRule("Q"),
            ManualRouteOffsetVarianceRatioRangeRule(5.3, 1187.0),
        ),
    )
}
