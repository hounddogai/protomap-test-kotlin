package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.text.normalize

/** A rule a lenient fuel surcharge preference must pass. Check returns its problems, or none. */
interface LenientFuelSurchargePreferenceRule {
    fun check(item: LenientFuelSurchargePreference): MutableList<String>
}

data class LenientFuelSurchargePreferenceLabelLengthRule(
    var maxLength: Int,
) : LenientFuelSurchargePreferenceRule {
    override fun check(item: LenientFuelSurchargePreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargePreferenceCodeLengthRule(
    var maxLength: Int,
) : LenientFuelSurchargePreferenceRule {
    override fun check(item: LenientFuelSurchargePreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargePreferenceRequiredDriverIdRule(
    var message: String,
) : LenientFuelSurchargePreferenceRule {
    override fun check(item: LenientFuelSurchargePreference): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargePreferenceAllowedDepotRule(
    var allowed: MutableList<String>,
) : LenientFuelSurchargePreferenceRule {
    override fun check(item: LenientFuelSurchargePreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargePreferenceRequiredLabelRule(
    var message: String,
) : LenientFuelSurchargePreferenceRule {
    override fun check(item: LenientFuelSurchargePreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargePreferenceRequiredDepotRule(
    var message: String,
) : LenientFuelSurchargePreferenceRule {
    override fun check(item: LenientFuelSurchargePreference): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient fuel surcharge preference. */
data class LenientFuelSurchargePreferenceValidator(var rules: MutableList<LenientFuelSurchargePreferenceRule>) {
    fun validate(item: LenientFuelSurchargePreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientFuelSurchargePreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFuelSurchargePreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFuelSurchargePreferenceValidator(): LenientFuelSurchargePreferenceValidator {
    return LenientFuelSurchargePreferenceValidator(
        mutableListOf<LenientFuelSurchargePreferenceRule>(
            LenientFuelSurchargePreferenceLabelLengthRule(3600),
            LenientFuelSurchargePreferenceCodeLengthRule(80),
            LenientFuelSurchargePreferenceRequiredDriverIdRule(
                "lenient fuel surcharge preference needs this field",
            ),
            LenientFuelSurchargePreferenceAllowedDepotRule(mutableListOf<String>("fallback")),
            LenientFuelSurchargePreferenceRequiredLabelRule("lenient fuel surcharge preference needs an entry"),
            LenientFuelSurchargePreferenceRequiredDepotRule(
                "lenient fuel surcharge preference needs this field",
            ),
        ),
    )
}
