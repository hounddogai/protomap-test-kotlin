package com.acme.transport.customs.tariff.tariffprofile

/** A rule a legacy tariff allocation must pass. Check returns its problems, or none. */
interface LegacyTariffAllocationRule {
    fun check(item: LegacyTariffAllocation): MutableList<String>
}

data class LegacyTariffAllocationUnitLengthRule(var maxLength: Int) : LegacyTariffAllocationRule {
    override fun check(item: LegacyTariffAllocation): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTariffAllocationRequiredCodeRule(var message: String) : LegacyTariffAllocationRule {
    override fun check(item: LegacyTariffAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTariffAllocationExpiresAtRangeRule(var min: Int, var max: Int) : LegacyTariffAllocationRule {
    override fun check(item: LegacyTariffAllocation): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tariff allocation. */
data class LegacyTariffAllocationValidator(var rules: MutableList<LegacyTariffAllocationRule>) {
    fun validate(item: LegacyTariffAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTariffAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTariffAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTariffAllocationValidator(): LegacyTariffAllocationValidator {
    return LegacyTariffAllocationValidator(
        mutableListOf<LegacyTariffAllocationRule>(
            LegacyTariffAllocationUnitLengthRule(64),
            LegacyTariffAllocationRequiredCodeRule("legacy tariff allocation needs a value"),
            LegacyTariffAllocationExpiresAtRangeRule(64, 86400),
        ),
    )
}
