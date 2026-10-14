package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge digest must pass. Check returns its problems, or none. */
interface FuelSurchargeDigestRule {
    fun check(item: FuelSurchargeDigest): MutableList<String>
}

data class FuelSurchargeDigestAllowedTitleRule(var allowed: MutableList<String>) : FuelSurchargeDigestRule {
    override fun check(item: FuelSurchargeDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeDigestRateRangeRule(var min: Double, var max: Double) : FuelSurchargeDigestRule {
    override fun check(item: FuelSurchargeDigest): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class FuelSurchargeDigestTitleLengthRule(var maxLength: Int) : FuelSurchargeDigestRule {
    override fun check(item: FuelSurchargeDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeDigestGraceDaysRangeRule(var min: Int, var max: Int) : FuelSurchargeDigestRule {
    override fun check(item: FuelSurchargeDigest): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class FuelSurchargeDigestCodeLengthRule(var maxLength: Int) : FuelSurchargeDigestRule {
    override fun check(item: FuelSurchargeDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeDigestCodePrefixRule(var prefix: String) : FuelSurchargeDigestRule {
    override fun check(item: FuelSurchargeDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge digest. */
data class FuelSurchargeDigestValidator(var rules: MutableList<FuelSurchargeDigestRule>) {
    fun validate(item: FuelSurchargeDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeDigestValidator(): FuelSurchargeDigestValidator {
    return FuelSurchargeDigestValidator(
        mutableListOf<FuelSurchargeDigestRule>(
            FuelSurchargeDigestAllowedTitleRule(mutableListOf<String>("lenient")),
            FuelSurchargeDigestRateRangeRule(4.4, 2005.5),
            FuelSurchargeDigestTitleLengthRule(80),
            FuelSurchargeDigestGraceDaysRangeRule(255, 255),
            FuelSurchargeDigestCodeLengthRule(80),
            FuelSurchargeDigestCodePrefixRule("OPS"),
        ),
    )
}
