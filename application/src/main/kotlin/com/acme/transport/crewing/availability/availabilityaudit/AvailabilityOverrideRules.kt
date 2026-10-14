package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.kit.text.normalize

/** A rule a availability override must pass. Check returns its problems, or none. */
interface AvailabilityOverrideRule {
    fun check(item: AvailabilityOverride): MutableList<String>
}

data class AvailabilityOverrideCodePrefixRule(var prefix: String) : AvailabilityOverrideRule {
    override fun check(item: AvailabilityOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityOverrideAllowedLabelRule(var allowed: MutableList<String>) : AvailabilityOverrideRule {
    override fun check(item: AvailabilityOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityOverrideRequiredCategoryRule(var message: String) : AvailabilityOverrideRule {
    override fun check(item: AvailabilityOverride): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability override. */
data class AvailabilityOverrideValidator(var rules: MutableList<AvailabilityOverrideRule>) {
    fun validate(item: AvailabilityOverride): MutableList<String> {
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

    fun requireValid(item: AvailabilityOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityOverrideValidator(): AvailabilityOverrideValidator {
    return AvailabilityOverrideValidator(
        mutableListOf<AvailabilityOverrideRule>(
            AvailabilityOverrideCodePrefixRule("X"),
            AvailabilityOverrideAllowedLabelRule(mutableListOf<String>()),
            AvailabilityOverrideRequiredCategoryRule("availability override needs an entry"),
        ),
    )
}
