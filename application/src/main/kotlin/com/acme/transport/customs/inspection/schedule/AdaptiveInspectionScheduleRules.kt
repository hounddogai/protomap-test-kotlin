package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.normalize

/** A rule a adaptive inspection schedule must pass. Check returns its problems, or none. */
interface AdaptiveInspectionScheduleRule {
    fun check(item: AdaptiveInspectionSchedule): MutableList<String>
}

data class AdaptiveInspectionScheduleOwnerIdLengthRule(var maxLength: Int) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionScheduleAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionScheduleNameLengthRule(var maxLength: Int) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionScheduleCodePrefixRule(var prefix: String) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionScheduleCostRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection schedule. */
data class AdaptiveInspectionScheduleValidator(var rules: MutableList<AdaptiveInspectionScheduleRule>) {
    fun validate(item: AdaptiveInspectionSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveInspectionSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionScheduleValidator(): AdaptiveInspectionScheduleValidator {
    return AdaptiveInspectionScheduleValidator(
        mutableListOf<AdaptiveInspectionScheduleRule>(
            AdaptiveInspectionScheduleOwnerIdLengthRule(16),
            AdaptiveInspectionScheduleAllowedCodeRule(mutableListOf<String>("default", "manual", "tiered")),
            AdaptiveInspectionScheduleNameLengthRule(255),
            AdaptiveInspectionScheduleCodePrefixRule("OPS"),
            AdaptiveInspectionScheduleCostRangeRule(8.4, 1014.0),
        ),
    )
}
