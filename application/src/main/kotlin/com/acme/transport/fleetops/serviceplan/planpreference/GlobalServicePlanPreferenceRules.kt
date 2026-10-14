package com.acme.transport.fleetops.serviceplan.planpreference

/** A rule a global service plan preference must pass. Check returns its problems, or none. */
interface GlobalServicePlanPreferenceRule {
    fun check(item: GlobalServicePlanPreference): MutableList<String>
}

data class GlobalServicePlanPreferenceNameLengthRule(var maxLength: Int) : GlobalServicePlanPreferenceRule {
    override fun check(item: GlobalServicePlanPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalServicePlanPreferenceCodePrefixRule(var prefix: String) : GlobalServicePlanPreferenceRule {
    override fun check(item: GlobalServicePlanPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalServicePlanPreferenceScoreRangeRule(
    var min: Double,
    var max: Double,
) : GlobalServicePlanPreferenceRule {
    override fun check(item: GlobalServicePlanPreference): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class GlobalServicePlanPreferenceRegionLengthRule(var maxLength: Int) : GlobalServicePlanPreferenceRule {
    override fun check(item: GlobalServicePlanPreference): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global service plan preference. */
data class GlobalServicePlanPreferenceValidator(var rules: MutableList<GlobalServicePlanPreferenceRule>) {
    fun validate(item: GlobalServicePlanPreference): MutableList<String> {
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

    fun requireValid(item: GlobalServicePlanPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalServicePlanPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalServicePlanPreferenceValidator(): GlobalServicePlanPreferenceValidator {
    return GlobalServicePlanPreferenceValidator(
        mutableListOf<GlobalServicePlanPreferenceRule>(
            GlobalServicePlanPreferenceNameLengthRule(120),
            GlobalServicePlanPreferenceCodePrefixRule("X"),
            GlobalServicePlanPreferenceScoreRangeRule(2.1, 1575.1),
            GlobalServicePlanPreferenceRegionLengthRule(255),
        ),
    )
}
