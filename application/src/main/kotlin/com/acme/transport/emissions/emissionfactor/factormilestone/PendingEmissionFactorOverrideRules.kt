package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.text.normalize

/** A rule a pending emission factor override must pass. Check returns its problems, or none. */
interface PendingEmissionFactorOverrideRule {
    fun check(item: PendingEmissionFactorOverride): MutableList<String>
}

data class PendingEmissionFactorOverrideCodePrefixRule(var prefix: String) : PendingEmissionFactorOverrideRule {
    override fun check(item: PendingEmissionFactorOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorOverrideRequiredDriverIdRule(
    var message: String,
) : PendingEmissionFactorOverrideRule {
    override fun check(item: PendingEmissionFactorOverride): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorOverrideAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : PendingEmissionFactorOverrideRule {
    override fun check(item: PendingEmissionFactorOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorOverrideNameLengthRule(var maxLength: Int) : PendingEmissionFactorOverrideRule {
    override fun check(item: PendingEmissionFactorOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorOverrideRequiredCodeRule(
    var message: String,
) : PendingEmissionFactorOverrideRule {
    override fun check(item: PendingEmissionFactorOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorOverrideLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : PendingEmissionFactorOverrideRule {
    override fun check(item: PendingEmissionFactorOverride): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending emission factor override. */
data class PendingEmissionFactorOverrideValidator(var rules: MutableList<PendingEmissionFactorOverrideRule>) {
    fun validate(item: PendingEmissionFactorOverride): MutableList<String> {
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

    fun requireValid(item: PendingEmissionFactorOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingEmissionFactorOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingEmissionFactorOverrideValidator(): PendingEmissionFactorOverrideValidator {
    return PendingEmissionFactorOverrideValidator(
        mutableListOf<PendingEmissionFactorOverrideRule>(
            PendingEmissionFactorOverrideCodePrefixRule("REF"),
            PendingEmissionFactorOverrideRequiredDriverIdRule("pending emission factor override needs a value"),
            PendingEmissionFactorOverrideAllowedDriverIdRule(
                mutableListOf<String>("scheduled", "strict", "batched"),
            ),
            PendingEmissionFactorOverrideNameLengthRule(80),
            PendingEmissionFactorOverrideRequiredCodeRule("pending emission factor override needs an entry"),
            PendingEmissionFactorOverrideLoadKgRangeRule(8.4, 4906.7),
        ),
    )
}
