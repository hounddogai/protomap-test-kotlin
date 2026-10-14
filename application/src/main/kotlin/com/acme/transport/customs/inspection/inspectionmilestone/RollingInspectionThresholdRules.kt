package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.text.normalize

/** A rule a rolling inspection threshold must pass. Check returns its problems, or none. */
interface RollingInspectionThresholdRule {
    fun check(item: RollingInspectionThreshold): MutableList<String>
}

data class RollingInspectionThresholdAllowedNameRule(
    var allowed: MutableList<String>,
) : RollingInspectionThresholdRule {
    override fun check(item: RollingInspectionThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionThresholdCodeLengthRule(var maxLength: Int) : RollingInspectionThresholdRule {
    override fun check(item: RollingInspectionThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionThresholdRequiredRouteCodeRule(
    var message: String,
) : RollingInspectionThresholdRule {
    override fun check(item: RollingInspectionThreshold): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionThresholdTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingInspectionThresholdRule {
    override fun check(item: RollingInspectionThreshold): MutableList<String> {
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

data class RollingInspectionThresholdRequiredCodeRule(var message: String) : RollingInspectionThresholdRule {
    override fun check(item: RollingInspectionThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionThresholdFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : RollingInspectionThresholdRule {
    override fun check(item: RollingInspectionThreshold): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling inspection threshold. */
data class RollingInspectionThresholdValidator(var rules: MutableList<RollingInspectionThresholdRule>) {
    fun validate(item: RollingInspectionThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingInspectionThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingInspectionThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingInspectionThresholdValidator(): RollingInspectionThresholdValidator {
    return RollingInspectionThresholdValidator(
        mutableListOf<RollingInspectionThresholdRule>(
            RollingInspectionThresholdAllowedNameRule(mutableListOf<String>("rolling")),
            RollingInspectionThresholdCodeLengthRule(32),
            RollingInspectionThresholdRequiredRouteCodeRule("rolling inspection threshold needs an entry"),
            RollingInspectionThresholdTagsLimitRule(16, mutableListOf<String>("global", "pending", "primary")),
            RollingInspectionThresholdRequiredCodeRule("rolling inspection threshold needs an entry"),
            RollingInspectionThresholdFloorValueRangeRule(5.4, 4424.0),
        ),
    )
}
