package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.normalize

/** A rule a default near miss override must pass. Check returns its problems, or none. */
interface DefaultNearMissOverrideRule {
    fun check(item: DefaultNearMissOverride): MutableList<String>
}

data class DefaultNearMissOverrideCodePrefixRule(var prefix: String) : DefaultNearMissOverrideRule {
    override fun check(item: DefaultNearMissOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissOverrideCapacityRangeRule(var min: Int, var max: Int) : DefaultNearMissOverrideRule {
    override fun check(item: DefaultNearMissOverride): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissOverrideAllowedLabelRule(
    var allowed: MutableList<String>,
) : DefaultNearMissOverrideRule {
    override fun check(item: DefaultNearMissOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissOverrideVehicleIdLengthRule(var maxLength: Int) : DefaultNearMissOverrideRule {
    override fun check(item: DefaultNearMissOverride): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default near miss override. */
data class DefaultNearMissOverrideValidator(var rules: MutableList<DefaultNearMissOverrideRule>) {
    fun validate(item: DefaultNearMissOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultNearMissOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultNearMissOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultNearMissOverrideValidator(): DefaultNearMissOverrideValidator {
    return DefaultNearMissOverrideValidator(
        mutableListOf<DefaultNearMissOverrideRule>(
            DefaultNearMissOverrideCodePrefixRule("REF"),
            DefaultNearMissOverrideCapacityRangeRule(255, 86400),
            DefaultNearMissOverrideAllowedLabelRule(mutableListOf<String>("default")),
            DefaultNearMissOverrideVehicleIdLengthRule(255),
        ),
    )
}
