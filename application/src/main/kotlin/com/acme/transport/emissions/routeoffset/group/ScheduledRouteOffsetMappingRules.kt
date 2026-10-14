package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.kit.text.normalize

/** A rule a scheduled route offset mapping must pass. Check returns its problems, or none. */
interface ScheduledRouteOffsetMappingRule {
    fun check(item: ScheduledRouteOffsetMapping): MutableList<String>
}

data class ScheduledRouteOffsetMappingQuantityRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledRouteOffsetMappingRule {
    override fun check(item: ScheduledRouteOffsetMapping): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ScheduledRouteOffsetMappingRequiredCategoryRule(
    var message: String,
) : ScheduledRouteOffsetMappingRule {
    override fun check(item: ScheduledRouteOffsetMapping): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetMappingRequiredCodeRule(var message: String) : ScheduledRouteOffsetMappingRule {
    override fun check(item: ScheduledRouteOffsetMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetMappingRequiredNameRule(var message: String) : ScheduledRouteOffsetMappingRule {
    override fun check(item: ScheduledRouteOffsetMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetMappingAllowedCategoryRule(
    var allowed: MutableList<String>,
) : ScheduledRouteOffsetMappingRule {
    override fun check(item: ScheduledRouteOffsetMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled route offset mapping. */
data class ScheduledRouteOffsetMappingValidator(var rules: MutableList<ScheduledRouteOffsetMappingRule>) {
    fun validate(item: ScheduledRouteOffsetMapping): MutableList<String> {
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

    fun requireValid(item: ScheduledRouteOffsetMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRouteOffsetMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRouteOffsetMappingValidator(): ScheduledRouteOffsetMappingValidator {
    return ScheduledRouteOffsetMappingValidator(
        mutableListOf<ScheduledRouteOffsetMappingRule>(
            ScheduledRouteOffsetMappingQuantityRangeRule(3600, 86400),
            ScheduledRouteOffsetMappingRequiredCategoryRule("scheduled route offset mapping needs this field"),
            ScheduledRouteOffsetMappingRequiredCodeRule("scheduled route offset mapping needs this field"),
            ScheduledRouteOffsetMappingRequiredNameRule("scheduled route offset mapping needs a value"),
            ScheduledRouteOffsetMappingAllowedCategoryRule(mutableListOf<String>()),
        ),
    )
}
