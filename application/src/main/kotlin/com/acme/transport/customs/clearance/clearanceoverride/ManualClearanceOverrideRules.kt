package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.normalize

/** A rule a manual clearance override must pass. Check returns its problems, or none. */
interface ManualClearanceOverrideRule {
    fun check(item: ManualClearanceOverride): MutableList<String>
}

data class ManualClearanceOverrideRequiredCodeRule(var message: String) : ManualClearanceOverrideRule {
    override fun check(item: ManualClearanceOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceOverrideCodeLengthRule(var maxLength: Int) : ManualClearanceOverrideRule {
    override fun check(item: ManualClearanceOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceOverrideCeilingRangeRule(
    var min: Double,
    var max: Double,
) : ManualClearanceOverrideRule {
    override fun check(item: ManualClearanceOverride): MutableList<String> {
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

data class ManualClearanceOverrideNameLengthRule(var maxLength: Int) : ManualClearanceOverrideRule {
    override fun check(item: ManualClearanceOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceOverrideRequiredNameRule(var message: String) : ManualClearanceOverrideRule {
    override fun check(item: ManualClearanceOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceOverrideAllowedSourceRule(
    var allowed: MutableList<String>,
) : ManualClearanceOverrideRule {
    override fun check(item: ManualClearanceOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual clearance override. */
data class ManualClearanceOverrideValidator(var rules: MutableList<ManualClearanceOverrideRule>) {
    fun validate(item: ManualClearanceOverride): MutableList<String> {
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

    fun requireValid(item: ManualClearanceOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualClearanceOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultManualClearanceOverrideValidator(): ManualClearanceOverrideValidator {
    return ManualClearanceOverrideValidator(
        mutableListOf<ManualClearanceOverrideRule>(
            ManualClearanceOverrideRequiredCodeRule("manual clearance override needs this field"),
            ManualClearanceOverrideCodeLengthRule(80),
            ManualClearanceOverrideCeilingRangeRule(2.7, 4317.5),
            ManualClearanceOverrideNameLengthRule(16),
            ManualClearanceOverrideRequiredNameRule("manual clearance override needs this field"),
            ManualClearanceOverrideAllowedSourceRule(mutableListOf<String>()),
        ),
    )
}
