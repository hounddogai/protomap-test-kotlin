package com.acme.transport.fleetops.tire.window

/** A rule a tire override must pass. Check returns its problems, or none. */
interface TireOverrideRule {
    fun check(item: TireOverride): MutableList<String>
}

data class TireOverrideRequiredContactEmailRule(var message: String) : TireOverrideRule {
    override fun check(item: TireOverride): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireOverrideLabelLengthRule(var maxLength: Int) : TireOverrideRule {
    override fun check(item: TireOverride): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireOverrideCodePrefixRule(var prefix: String) : TireOverrideRule {
    override fun check(item: TireOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire override. */
data class TireOverrideValidator(var rules: MutableList<TireOverrideRule>) {
    fun validate(item: TireOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTireOverrideValidator(): TireOverrideValidator {
    return TireOverrideValidator(
        mutableListOf<TireOverrideRule>(
            TireOverrideRequiredContactEmailRule("tire override needs a value"),
            TireOverrideLabelLengthRule(255),
            TireOverrideCodePrefixRule("X"),
        ),
    )
}
