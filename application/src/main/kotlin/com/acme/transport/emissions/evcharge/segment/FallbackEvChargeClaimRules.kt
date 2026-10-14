package com.acme.transport.emissions.evcharge.segment

/** A rule a fallback ev charge claim must pass. Check returns its problems, or none. */
interface FallbackEvChargeClaimRule {
    fun check(item: FallbackEvChargeClaim): MutableList<String>
}

data class FallbackEvChargeClaimLimitRangeRule(var min: Int, var max: Int) : FallbackEvChargeClaimRule {
    override fun check(item: FallbackEvChargeClaim): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeClaimTitleLengthRule(var maxLength: Int) : FallbackEvChargeClaimRule {
    override fun check(item: FallbackEvChargeClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeClaimRequiredExternalRefRule(var message: String) : FallbackEvChargeClaimRule {
    override fun check(item: FallbackEvChargeClaim): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback ev charge claim. */
data class FallbackEvChargeClaimValidator(var rules: MutableList<FallbackEvChargeClaimRule>) {
    fun validate(item: FallbackEvChargeClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackEvChargeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackEvChargeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackEvChargeClaimValidator(): FallbackEvChargeClaimValidator {
    return FallbackEvChargeClaimValidator(
        mutableListOf<FallbackEvChargeClaimRule>(
            FallbackEvChargeClaimLimitRangeRule(64, 255),
            FallbackEvChargeClaimTitleLengthRule(80),
            FallbackEvChargeClaimRequiredExternalRefRule("fallback ev charge claim needs a value"),
        ),
    )
}
