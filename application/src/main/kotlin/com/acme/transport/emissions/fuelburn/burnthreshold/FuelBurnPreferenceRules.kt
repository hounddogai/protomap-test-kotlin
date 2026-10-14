package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn preference must pass. Check returns its problems, or none. */
interface FuelBurnPreferenceRule {
    fun check(item: FuelBurnPreference): MutableList<String>
}

data class FuelBurnPreferenceRequiredShiftCodeRule(var message: String) : FuelBurnPreferenceRule {
    override fun check(item: FuelBurnPreference): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnPreferenceAllowedCodeRule(var allowed: MutableList<String>) : FuelBurnPreferenceRule {
    override fun check(item: FuelBurnPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnPreferenceShiftCodeLengthRule(var maxLength: Int) : FuelBurnPreferenceRule {
    override fun check(item: FuelBurnPreference): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnPreferenceDriverIdLengthRule(var maxLength: Int) : FuelBurnPreferenceRule {
    override fun check(item: FuelBurnPreference): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn preference. */
data class FuelBurnPreferenceValidator(var rules: MutableList<FuelBurnPreferenceRule>) {
    fun validate(item: FuelBurnPreference): MutableList<String> {
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

    fun requireValid(item: FuelBurnPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnPreferenceValidator(): FuelBurnPreferenceValidator {
    return FuelBurnPreferenceValidator(
        mutableListOf<FuelBurnPreferenceRule>(
            FuelBurnPreferenceRequiredShiftCodeRule("fuel burn preference needs a value"),
            FuelBurnPreferenceAllowedCodeRule(mutableListOf<String>()),
            FuelBurnPreferenceShiftCodeLengthRule(3600),
            FuelBurnPreferenceDriverIdLengthRule(1000),
        ),
    )
}
