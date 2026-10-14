package com.acme.transport.customs.tariff.policy

/** A rule a lenient tariff policy must pass. Check returns its problems, or none. */
interface LenientTariffPolicyRule {
    fun check(item: LenientTariffPolicy): MutableList<String>
}

data class LenientTariffPolicyPhoneLengthRule(var maxLength: Int) : LenientTariffPolicyRule {
    override fun check(item: LenientTariffPolicy): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffPolicyPalletsRangeRule(var min: Int, var max: Int) : LenientTariffPolicyRule {
    override fun check(item: LenientTariffPolicy): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class LenientTariffPolicyRequiredPhoneRule(var message: String) : LenientTariffPolicyRule {
    override fun check(item: LenientTariffPolicy): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient tariff policy. */
data class LenientTariffPolicyValidator(var rules: MutableList<LenientTariffPolicyRule>) {
    fun validate(item: LenientTariffPolicy): MutableList<String> {
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

    fun requireValid(item: LenientTariffPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTariffPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTariffPolicyValidator(): LenientTariffPolicyValidator {
    return LenientTariffPolicyValidator(
        mutableListOf<LenientTariffPolicyRule>(
            LenientTariffPolicyPhoneLengthRule(64),
            LenientTariffPolicyPalletsRangeRule(120, 86400),
            LenientTariffPolicyRequiredPhoneRule("lenient tariff policy needs this field"),
        ),
    )
}
