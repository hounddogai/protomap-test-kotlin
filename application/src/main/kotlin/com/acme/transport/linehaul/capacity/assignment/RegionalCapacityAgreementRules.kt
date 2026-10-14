package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.kit.text.normalize

/** A rule a regional capacity agreement must pass. Check returns its problems, or none. */
interface RegionalCapacityAgreementRule {
    fun check(item: RegionalCapacityAgreement): MutableList<String>
}

data class RegionalCapacityAgreementQuantityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCapacityAgreementRule {
    override fun check(item: RegionalCapacityAgreement): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityAgreementRequiredVehicleIdRule(var message: String) : RegionalCapacityAgreementRule {
    override fun check(item: RegionalCapacityAgreement): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityAgreementRequiredCodeRule(var message: String) : RegionalCapacityAgreementRule {
    override fun check(item: RegionalCapacityAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityAgreementAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalCapacityAgreementRule {
    override fun check(item: RegionalCapacityAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityAgreementFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RegionalCapacityAgreementRule {
    override fun check(item: RegionalCapacityAgreement): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityAgreementVehicleIdLengthRule(var maxLength: Int) : RegionalCapacityAgreementRule {
    override fun check(item: RegionalCapacityAgreement): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional capacity agreement. */
data class RegionalCapacityAgreementValidator(var rules: MutableList<RegionalCapacityAgreementRule>) {
    fun validate(item: RegionalCapacityAgreement): MutableList<String> {
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

    fun requireValid(item: RegionalCapacityAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCapacityAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCapacityAgreementValidator(): RegionalCapacityAgreementValidator {
    return RegionalCapacityAgreementValidator(
        mutableListOf<RegionalCapacityAgreementRule>(
            RegionalCapacityAgreementQuantityRangeRule(64, 120),
            RegionalCapacityAgreementRequiredVehicleIdRule("regional capacity agreement needs this field"),
            RegionalCapacityAgreementRequiredCodeRule("regional capacity agreement needs this field"),
            RegionalCapacityAgreementAllowedCodeRule(mutableListOf<String>("legacy", "fallback", "strict")),
            RegionalCapacityAgreementFuelLitersRangeRule(5.9, 4996.6),
            RegionalCapacityAgreementVehicleIdLengthRule(64),
        ),
    )
}
