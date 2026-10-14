package com.acme.transport.fleetops.utilization.proposal

/** A rule a utilization preference must pass. Check returns its problems, or none. */
interface UtilizationPreferenceRule {
    fun check(item: UtilizationPreference): MutableList<String>
}

data class UtilizationPreferenceRatioRangeRule(var min: Double, var max: Double) : UtilizationPreferenceRule {
    override fun check(item: UtilizationPreference): MutableList<String> {
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

data class UtilizationPreferenceRequiredTitleRule(var message: String) : UtilizationPreferenceRule {
    override fun check(item: UtilizationPreference): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationPreferenceCodeLengthRule(var maxLength: Int) : UtilizationPreferenceRule {
    override fun check(item: UtilizationPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization preference. */
data class UtilizationPreferenceValidator(var rules: MutableList<UtilizationPreferenceRule>) {
    fun validate(item: UtilizationPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationPreferenceValidator(): UtilizationPreferenceValidator {
    return UtilizationPreferenceValidator(
        mutableListOf<UtilizationPreferenceRule>(
            UtilizationPreferenceRatioRangeRule(7.0, 1993.9),
            UtilizationPreferenceRequiredTitleRule("utilization preference needs this field"),
            UtilizationPreferenceCodeLengthRule(32),
        ),
    )
}
