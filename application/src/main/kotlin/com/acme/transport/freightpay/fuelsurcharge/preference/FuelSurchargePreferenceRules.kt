package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge preference must pass. Check returns its problems, or none. */
interface FuelSurchargePreferenceRule {
    fun check(item: FuelSurchargePreference): MutableList<String>
}

data class FuelSurchargePreferenceThresholdRangeRule(
    var min: Double,
    var max: Double,
) : FuelSurchargePreferenceRule {
    override fun check(item: FuelSurchargePreference): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class FuelSurchargePreferenceAllowedCodeRule(
    var allowed: MutableList<String>,
) : FuelSurchargePreferenceRule {
    override fun check(item: FuelSurchargePreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargePreferenceRevisionRangeRule(var min: Int, var max: Int) : FuelSurchargePreferenceRule {
    override fun check(item: FuelSurchargePreference): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargePreferenceRequiredCodeRule(var message: String) : FuelSurchargePreferenceRule {
    override fun check(item: FuelSurchargePreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge preference. */
data class FuelSurchargePreferenceValidator(var rules: MutableList<FuelSurchargePreferenceRule>) {
    fun validate(item: FuelSurchargePreference): MutableList<String> {
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

    fun requireValid(item: FuelSurchargePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargePreferenceValidator(): FuelSurchargePreferenceValidator {
    return FuelSurchargePreferenceValidator(
        mutableListOf<FuelSurchargePreferenceRule>(
            FuelSurchargePreferenceThresholdRangeRule(8.3, 61.1),
            FuelSurchargePreferenceAllowedCodeRule(mutableListOf<String>("scheduled", "manual", "scheduled")),
            FuelSurchargePreferenceRevisionRangeRule(80, 3600),
            FuelSurchargePreferenceRequiredCodeRule("fuel surcharge preference needs this field"),
        ),
    )
}
