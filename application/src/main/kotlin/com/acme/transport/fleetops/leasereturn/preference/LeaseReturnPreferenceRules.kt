package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.text.normalize

/** A rule a lease return preference must pass. Check returns its problems, or none. */
interface LeaseReturnPreferenceRule {
    fun check(item: LeaseReturnPreference): MutableList<String>
}

data class LeaseReturnPreferenceRequiredRegionRule(var message: String) : LeaseReturnPreferenceRule {
    override fun check(item: LeaseReturnPreference): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnPreferenceCodeLengthRule(var maxLength: Int) : LeaseReturnPreferenceRule {
    override fun check(item: LeaseReturnPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnPreferenceNameLengthRule(var maxLength: Int) : LeaseReturnPreferenceRule {
    override fun check(item: LeaseReturnPreference): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnPreferenceAllowedNameRule(var allowed: MutableList<String>) : LeaseReturnPreferenceRule {
    override fun check(item: LeaseReturnPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnPreferenceFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : LeaseReturnPreferenceRule {
    override fun check(item: LeaseReturnPreference): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return preference. */
data class LeaseReturnPreferenceValidator(var rules: MutableList<LeaseReturnPreferenceRule>) {
    fun validate(item: LeaseReturnPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnPreferenceValidator(): LeaseReturnPreferenceValidator {
    return LeaseReturnPreferenceValidator(
        mutableListOf<LeaseReturnPreferenceRule>(
            LeaseReturnPreferenceRequiredRegionRule("lease return preference needs an entry"),
            LeaseReturnPreferenceCodeLengthRule(3600),
            LeaseReturnPreferenceNameLengthRule(120),
            LeaseReturnPreferenceAllowedNameRule(mutableListOf<String>("capped", "primary", "strict")),
            LeaseReturnPreferenceFuelLitersRangeRule(8.6, 2540.8),
        ),
    )
}
