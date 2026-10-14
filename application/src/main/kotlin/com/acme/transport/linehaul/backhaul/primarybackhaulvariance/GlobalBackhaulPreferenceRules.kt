package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

/** A rule a global backhaul preference must pass. Check returns its problems, or none. */
interface GlobalBackhaulPreferenceRule {
    fun check(item: GlobalBackhaulPreference): MutableList<String>
}

data class GlobalBackhaulPreferencePriorityRangeRule(
    var min: Int,
    var max: Int,
) : GlobalBackhaulPreferenceRule {
    override fun check(item: GlobalBackhaulPreference): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class GlobalBackhaulPreferenceRequiredCodeRule(var message: String) : GlobalBackhaulPreferenceRule {
    override fun check(item: GlobalBackhaulPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalBackhaulPreferenceCodeLengthRule(var maxLength: Int) : GlobalBackhaulPreferenceRule {
    override fun check(item: GlobalBackhaulPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global backhaul preference. */
data class GlobalBackhaulPreferenceValidator(var rules: MutableList<GlobalBackhaulPreferenceRule>) {
    fun validate(item: GlobalBackhaulPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalBackhaulPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalBackhaulPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalBackhaulPreferenceValidator(): GlobalBackhaulPreferenceValidator {
    return GlobalBackhaulPreferenceValidator(
        mutableListOf<GlobalBackhaulPreferenceRule>(
            GlobalBackhaulPreferencePriorityRangeRule(80, 120),
            GlobalBackhaulPreferenceRequiredCodeRule("global backhaul preference needs an entry"),
            GlobalBackhaulPreferenceCodeLengthRule(3600),
        ),
    )
}
