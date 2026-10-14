package com.acme.transport.fleetops.utilization.exception

/** A rule a default utilization preference must pass. Check returns its problems, or none. */
interface DefaultUtilizationPreferenceRule {
    fun check(item: DefaultUtilizationPreference): MutableList<String>
}

data class DefaultUtilizationPreferenceRequiredCodeRule(
    var message: String,
) : DefaultUtilizationPreferenceRule {
    override fun check(item: DefaultUtilizationPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultUtilizationPreferenceSourceLengthRule(var maxLength: Int) : DefaultUtilizationPreferenceRule {
    override fun check(item: DefaultUtilizationPreference): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultUtilizationPreferenceCodeLengthRule(var maxLength: Int) : DefaultUtilizationPreferenceRule {
    override fun check(item: DefaultUtilizationPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultUtilizationPreferenceNameLengthRule(var maxLength: Int) : DefaultUtilizationPreferenceRule {
    override fun check(item: DefaultUtilizationPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultUtilizationPreferenceRequiredSourceRule(
    var message: String,
) : DefaultUtilizationPreferenceRule {
    override fun check(item: DefaultUtilizationPreference): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default utilization preference. */
data class DefaultUtilizationPreferenceValidator(var rules: MutableList<DefaultUtilizationPreferenceRule>) {
    fun validate(item: DefaultUtilizationPreference): MutableList<String> {
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

    fun requireValid(item: DefaultUtilizationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultUtilizationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultUtilizationPreferenceValidator(): DefaultUtilizationPreferenceValidator {
    return DefaultUtilizationPreferenceValidator(
        mutableListOf<DefaultUtilizationPreferenceRule>(
            DefaultUtilizationPreferenceRequiredCodeRule("default utilization preference needs this field"),
            DefaultUtilizationPreferenceSourceLengthRule(3600),
            DefaultUtilizationPreferenceCodeLengthRule(32),
            DefaultUtilizationPreferenceNameLengthRule(80),
            DefaultUtilizationPreferenceRequiredSourceRule("default utilization preference needs a value"),
        ),
    )
}
