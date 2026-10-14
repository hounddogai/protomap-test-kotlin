package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.text.normalize

/** A rule a global swap override must pass. Check returns its problems, or none. */
interface GlobalSwapOverrideRule {
    fun check(item: GlobalSwapOverride): MutableList<String>
}

data class GlobalSwapOverrideRequiredVehicleIdRule(var message: String) : GlobalSwapOverrideRule {
    override fun check(item: GlobalSwapOverride): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalSwapOverrideAllowedVehicleIdRule(var allowed: MutableList<String>) : GlobalSwapOverrideRule {
    override fun check(item: GlobalSwapOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSwapOverrideRequiredCodeRule(var message: String) : GlobalSwapOverrideRule {
    override fun check(item: GlobalSwapOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalSwapOverrideRetriesRangeRule(var min: Int, var max: Int) : GlobalSwapOverrideRule {
    override fun check(item: GlobalSwapOverride): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalSwapOverrideLabelLengthRule(var maxLength: Int) : GlobalSwapOverrideRule {
    override fun check(item: GlobalSwapOverride): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global swap override. */
data class GlobalSwapOverrideValidator(var rules: MutableList<GlobalSwapOverrideRule>) {
    fun validate(item: GlobalSwapOverride): MutableList<String> {
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

    fun requireValid(item: GlobalSwapOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalSwapOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalSwapOverrideValidator(): GlobalSwapOverrideValidator {
    return GlobalSwapOverrideValidator(
        mutableListOf<GlobalSwapOverrideRule>(
            GlobalSwapOverrideRequiredVehicleIdRule("global swap override needs this field"),
            GlobalSwapOverrideAllowedVehicleIdRule(mutableListOf<String>("rolling", "batched", "default")),
            GlobalSwapOverrideRequiredCodeRule("global swap override needs an entry"),
            GlobalSwapOverrideRetriesRangeRule(120, 3600),
            GlobalSwapOverrideLabelLengthRule(120),
        ),
    )
}
