package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.kit.text.normalize

/** A rule a lenient ev charge preference must pass. Check returns its problems, or none. */
interface LenientEvChargePreferenceRule {
    fun check(item: LenientEvChargePreference): MutableList<String>
}

data class LenientEvChargePreferenceAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : LenientEvChargePreferenceRule {
    override fun check(item: LenientEvChargePreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LenientEvChargePreferenceCodePrefixRule(var prefix: String) : LenientEvChargePreferenceRule {
    override fun check(item: LenientEvChargePreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientEvChargePreferenceVehicleIdLengthRule(var maxLength: Int) : LenientEvChargePreferenceRule {
    override fun check(item: LenientEvChargePreference): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientEvChargePreferenceRequiredLabelRule(var message: String) : LenientEvChargePreferenceRule {
    override fun check(item: LenientEvChargePreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientEvChargePreferenceRequiredCodeRule(var message: String) : LenientEvChargePreferenceRule {
    override fun check(item: LenientEvChargePreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientEvChargePreferenceRequiredVehicleIdRule(var message: String) : LenientEvChargePreferenceRule {
    override fun check(item: LenientEvChargePreference): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient ev charge preference. */
data class LenientEvChargePreferenceValidator(var rules: MutableList<LenientEvChargePreferenceRule>) {
    fun validate(item: LenientEvChargePreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientEvChargePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientEvChargePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientEvChargePreferenceValidator(): LenientEvChargePreferenceValidator {
    return LenientEvChargePreferenceValidator(
        mutableListOf<LenientEvChargePreferenceRule>(
            LenientEvChargePreferenceAllowedVehicleIdRule(mutableListOf<String>("rolling", "capped")),
            LenientEvChargePreferenceCodePrefixRule("Q"),
            LenientEvChargePreferenceVehicleIdLengthRule(86400),
            LenientEvChargePreferenceRequiredLabelRule("lenient ev charge preference needs an entry"),
            LenientEvChargePreferenceRequiredCodeRule("lenient ev charge preference needs a value"),
            LenientEvChargePreferenceRequiredVehicleIdRule("lenient ev charge preference needs an entry"),
        ),
    )
}
