package com.acme.transport.fleetops.serviceplan.target

/** A rule a pending service plan preference must pass. Check returns its problems, or none. */
interface PendingServicePlanPreferenceRule {
    fun check(item: PendingServicePlanPreference): MutableList<String>
}

data class PendingServicePlanPreferenceTitleLengthRule(var maxLength: Int) : PendingServicePlanPreferenceRule {
    override fun check(item: PendingServicePlanPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanPreferenceCeilingRangeRule(
    var min: Double,
    var max: Double,
) : PendingServicePlanPreferenceRule {
    override fun check(item: PendingServicePlanPreference): MutableList<String> {
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

data class PendingServicePlanPreferenceCodePrefixRule(var prefix: String) : PendingServicePlanPreferenceRule {
    override fun check(item: PendingServicePlanPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending service plan preference. */
data class PendingServicePlanPreferenceValidator(var rules: MutableList<PendingServicePlanPreferenceRule>) {
    fun validate(item: PendingServicePlanPreference): MutableList<String> {
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

    fun requireValid(item: PendingServicePlanPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingServicePlanPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingServicePlanPreferenceValidator(): PendingServicePlanPreferenceValidator {
    return PendingServicePlanPreferenceValidator(
        mutableListOf<PendingServicePlanPreferenceRule>(
            PendingServicePlanPreferenceTitleLengthRule(16),
            PendingServicePlanPreferenceCeilingRangeRule(0.2, 1423.3),
            PendingServicePlanPreferenceCodePrefixRule("ACM"),
        ),
    )
}
