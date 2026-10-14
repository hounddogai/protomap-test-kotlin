package com.acme.transport.customs.tariff.policy

/** A rule a lenient tariff claim must pass. Check returns its problems, or none. */
interface LenientTariffClaimRule {
    fun check(item: LenientTariffClaim): MutableList<String>
}

data class LenientTariffClaimRequiredCodeRule(var message: String) : LenientTariffClaimRule {
    override fun check(item: LenientTariffClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffClaimPhoneLengthRule(var maxLength: Int) : LenientTariffClaimRule {
    override fun check(item: LenientTariffClaim): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffClaimQuantityRangeRule(var min: Int, var max: Int) : LenientTariffClaimRule {
    override fun check(item: LenientTariffClaim): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class LenientTariffClaimCodeLengthRule(var maxLength: Int) : LenientTariffClaimRule {
    override fun check(item: LenientTariffClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffClaimCodePrefixRule(var prefix: String) : LenientTariffClaimRule {
    override fun check(item: LenientTariffClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientTariffClaimTitleLengthRule(var maxLength: Int) : LenientTariffClaimRule {
    override fun check(item: LenientTariffClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient tariff claim. */
data class LenientTariffClaimValidator(var rules: MutableList<LenientTariffClaimRule>) {
    fun validate(item: LenientTariffClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientTariffClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTariffClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTariffClaimValidator(): LenientTariffClaimValidator {
    return LenientTariffClaimValidator(
        mutableListOf<LenientTariffClaimRule>(
            LenientTariffClaimRequiredCodeRule("lenient tariff claim needs an entry"),
            LenientTariffClaimPhoneLengthRule(3600),
            LenientTariffClaimQuantityRangeRule(16, 80),
            LenientTariffClaimCodeLengthRule(1000),
            LenientTariffClaimCodePrefixRule("REF"),
            LenientTariffClaimTitleLengthRule(80),
        ),
    )
}
