package com.acme.transport.emissions.evcharge.proposal

/** A rule a ev charge override must pass. Check returns its problems, or none. */
interface EvChargeOverrideRule {
    fun check(item: EvChargeOverride): MutableList<String>
}

data class EvChargeOverrideCodePrefixRule(var prefix: String) : EvChargeOverrideRule {
    override fun check(item: EvChargeOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeOverrideLabelLengthRule(var maxLength: Int) : EvChargeOverrideRule {
    override fun check(item: EvChargeOverride): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeOverrideRequiredRegionRule(var message: String) : EvChargeOverrideRule {
    override fun check(item: EvChargeOverride): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge override. */
data class EvChargeOverrideValidator(var rules: MutableList<EvChargeOverrideRule>) {
    fun validate(item: EvChargeOverride): MutableList<String> {
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

    fun requireValid(item: EvChargeOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeOverrideValidator(): EvChargeOverrideValidator {
    return EvChargeOverrideValidator(
        mutableListOf<EvChargeOverrideRule>(
            EvChargeOverrideCodePrefixRule("ACM"),
            EvChargeOverrideLabelLengthRule(16),
            EvChargeOverrideRequiredRegionRule("ev charge override needs this field"),
        ),
    )
}
