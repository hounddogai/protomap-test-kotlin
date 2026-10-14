package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.kit.text.normalize

/** A rule a global utilization schedule must pass. Check returns its problems, or none. */
interface GlobalUtilizationScheduleRule {
    fun check(item: GlobalUtilizationSchedule): MutableList<String>
}

data class GlobalUtilizationScheduleAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalUtilizationScheduleRule {
    override fun check(item: GlobalUtilizationSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationScheduleCodePrefixRule(var prefix: String) : GlobalUtilizationScheduleRule {
    override fun check(item: GlobalUtilizationSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationScheduleMinimumRangeRule(
    var min: Double,
    var max: Double,
) : GlobalUtilizationScheduleRule {
    override fun check(item: GlobalUtilizationSchedule): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class GlobalUtilizationScheduleRequiredTitleRule(var message: String) : GlobalUtilizationScheduleRule {
    override fun check(item: GlobalUtilizationSchedule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationScheduleVersionRangeRule(
    var min: Int,
    var max: Int,
) : GlobalUtilizationScheduleRule {
    override fun check(item: GlobalUtilizationSchedule): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global utilization schedule. */
data class GlobalUtilizationScheduleValidator(var rules: MutableList<GlobalUtilizationScheduleRule>) {
    fun validate(item: GlobalUtilizationSchedule): MutableList<String> {
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

    fun requireValid(item: GlobalUtilizationSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalUtilizationSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalUtilizationScheduleValidator(): GlobalUtilizationScheduleValidator {
    return GlobalUtilizationScheduleValidator(
        mutableListOf<GlobalUtilizationScheduleRule>(
            GlobalUtilizationScheduleAllowedCodeRule(mutableListOf<String>()),
            GlobalUtilizationScheduleCodePrefixRule("X"),
            GlobalUtilizationScheduleMinimumRangeRule(1.1, 2753.3),
            GlobalUtilizationScheduleRequiredTitleRule("global utilization schedule needs this field"),
            GlobalUtilizationScheduleVersionRangeRule(120, 255),
        ),
    )
}
