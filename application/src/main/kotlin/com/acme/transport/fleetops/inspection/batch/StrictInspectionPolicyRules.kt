package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.kit.text.normalize

/** A rule a strict inspection policy must pass. Check returns its problems, or none. */
interface StrictInspectionPolicyRule {
    fun check(item: StrictInspectionPolicy): MutableList<String>
}

data class StrictInspectionPolicyRequiredCodeRule(var message: String) : StrictInspectionPolicyRule {
    override fun check(item: StrictInspectionPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionPolicyCodePrefixRule(var prefix: String) : StrictInspectionPolicyRule {
    override fun check(item: StrictInspectionPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionPolicyAllowedLocaleRule(
    var allowed: MutableList<String>,
) : StrictInspectionPolicyRule {
    override fun check(item: StrictInspectionPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict inspection policy. */
data class StrictInspectionPolicyValidator(var rules: MutableList<StrictInspectionPolicyRule>) {
    fun validate(item: StrictInspectionPolicy): MutableList<String> {
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

    fun requireValid(item: StrictInspectionPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictInspectionPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictInspectionPolicyValidator(): StrictInspectionPolicyValidator {
    return StrictInspectionPolicyValidator(
        mutableListOf<StrictInspectionPolicyRule>(
            StrictInspectionPolicyRequiredCodeRule("strict inspection policy needs an entry"),
            StrictInspectionPolicyCodePrefixRule("REF"),
            StrictInspectionPolicyAllowedLocaleRule(mutableListOf<String>("scheduled", "seasonal", "adaptive")),
        ),
    )
}
