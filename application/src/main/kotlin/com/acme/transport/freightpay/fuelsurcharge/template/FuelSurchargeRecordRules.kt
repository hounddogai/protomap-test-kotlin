package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge record must pass. Check returns its problems, or none. */
interface FuelSurchargeRecordRule {
    fun check(item: FuelSurchargeRecord): MutableList<String>
}

data class FuelSurchargeRecordCeilingRangeRule(var min: Double, var max: Double) : FuelSurchargeRecordRule {
    override fun check(item: FuelSurchargeRecord): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRecordCodePrefixRule(var prefix: String) : FuelSurchargeRecordRule {
    override fun check(item: FuelSurchargeRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRecordNameLengthRule(var maxLength: Int) : FuelSurchargeRecordRule {
    override fun check(item: FuelSurchargeRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRecordAllowedNameRule(var allowed: MutableList<String>) : FuelSurchargeRecordRule {
    override fun check(item: FuelSurchargeRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRecordDepotLengthRule(var maxLength: Int) : FuelSurchargeRecordRule {
    override fun check(item: FuelSurchargeRecord): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRecordRequiredExternalRefRule(var message: String) : FuelSurchargeRecordRule {
    override fun check(item: FuelSurchargeRecord): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge record. */
data class FuelSurchargeRecordValidator(var rules: MutableList<FuelSurchargeRecordRule>) {
    fun validate(item: FuelSurchargeRecord): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeRecordValidator(): FuelSurchargeRecordValidator {
    return FuelSurchargeRecordValidator(
        mutableListOf<FuelSurchargeRecordRule>(
            FuelSurchargeRecordCeilingRangeRule(7.7, 4137.4),
            FuelSurchargeRecordCodePrefixRule("Q"),
            FuelSurchargeRecordNameLengthRule(255),
            FuelSurchargeRecordAllowedNameRule(mutableListOf<String>("fallback", "scheduled")),
            FuelSurchargeRecordDepotLengthRule(16),
            FuelSurchargeRecordRequiredExternalRefRule("fuel surcharge record needs an entry"),
        ),
    )
}
