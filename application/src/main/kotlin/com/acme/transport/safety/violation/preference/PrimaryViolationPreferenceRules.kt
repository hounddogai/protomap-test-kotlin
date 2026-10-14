package com.acme.transport.safety.violation.preference

/** A rule a primary violation preference must pass. Check returns its problems, or none. */
interface PrimaryViolationPreferenceRule {
    fun check(item: PrimaryViolationPreference): MutableList<String>
}

data class PrimaryViolationPreferenceRequiredCodeRule(var message: String) : PrimaryViolationPreferenceRule {
    override fun check(item: PrimaryViolationPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryViolationPreferenceEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryViolationPreferenceRule {
    override fun check(item: PrimaryViolationPreference): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryViolationPreferenceCodePrefixRule(var prefix: String) : PrimaryViolationPreferenceRule {
    override fun check(item: PrimaryViolationPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary violation preference. */
data class PrimaryViolationPreferenceValidator(var rules: MutableList<PrimaryViolationPreferenceRule>) {
    fun validate(item: PrimaryViolationPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryViolationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryViolationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryViolationPreferenceValidator(): PrimaryViolationPreferenceValidator {
    return PrimaryViolationPreferenceValidator(
        mutableListOf<PrimaryViolationPreferenceRule>(
            PrimaryViolationPreferenceRequiredCodeRule("primary violation preference needs this field"),
            PrimaryViolationPreferenceEtaMinutesRangeRule(3600, 3600),
            PrimaryViolationPreferenceCodePrefixRule("ACM"),
        ),
    )
}
