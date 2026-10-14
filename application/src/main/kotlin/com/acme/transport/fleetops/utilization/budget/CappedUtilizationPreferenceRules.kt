package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.text.normalize

/** A rule a capped utilization preference must pass. Check returns its problems, or none. */
interface CappedUtilizationPreferenceRule {
    fun check(item: CappedUtilizationPreference): MutableList<String>
}

data class CappedUtilizationPreferenceAllowedCodeRule(
    var allowed: MutableList<String>,
) : CappedUtilizationPreferenceRule {
    override fun check(item: CappedUtilizationPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CappedUtilizationPreferenceNameLengthRule(var maxLength: Int) : CappedUtilizationPreferenceRule {
    override fun check(item: CappedUtilizationPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedUtilizationPreferenceCodePrefixRule(var prefix: String) : CappedUtilizationPreferenceRule {
    override fun check(item: CappedUtilizationPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedUtilizationPreferenceRequiredCodeRule(var message: String) : CappedUtilizationPreferenceRule {
    override fun check(item: CappedUtilizationPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped utilization preference. */
data class CappedUtilizationPreferenceValidator(var rules: MutableList<CappedUtilizationPreferenceRule>) {
    fun validate(item: CappedUtilizationPreference): MutableList<String> {
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

    fun requireValid(item: CappedUtilizationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedUtilizationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedUtilizationPreferenceValidator(): CappedUtilizationPreferenceValidator {
    return CappedUtilizationPreferenceValidator(
        mutableListOf<CappedUtilizationPreferenceRule>(
            CappedUtilizationPreferenceAllowedCodeRule(mutableListOf<String>("lenient", "global", "pending")),
            CappedUtilizationPreferenceNameLengthRule(255),
            CappedUtilizationPreferenceCodePrefixRule("Q"),
            CappedUtilizationPreferenceRequiredCodeRule("capped utilization preference needs a value"),
        ),
    )
}
