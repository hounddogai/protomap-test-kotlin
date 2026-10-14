package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge score must pass. Check returns its problems, or none. */
interface FuelSurchargeScoreRule {
    fun check(item: FuelSurchargeScore): MutableList<String>
}

data class FuelSurchargeScoreCodePrefixRule(var prefix: String) : FuelSurchargeScoreRule {
    override fun check(item: FuelSurchargeScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeScoreAllowedRegionRule(var allowed: MutableList<String>) : FuelSurchargeScoreRule {
    override fun check(item: FuelSurchargeScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is too long")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeScoreCodeLengthRule(var maxLength: Int) : FuelSurchargeScoreRule {
    override fun check(item: FuelSurchargeScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeScoreNameLengthRule(var maxLength: Int) : FuelSurchargeScoreRule {
    override fun check(item: FuelSurchargeScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeScoreSourceLengthRule(var maxLength: Int) : FuelSurchargeScoreRule {
    override fun check(item: FuelSurchargeScore): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge score. */
data class FuelSurchargeScoreValidator(var rules: MutableList<FuelSurchargeScoreRule>) {
    fun validate(item: FuelSurchargeScore): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeScore>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeScoreValidator(): FuelSurchargeScoreValidator {
    return FuelSurchargeScoreValidator(
        mutableListOf<FuelSurchargeScoreRule>(
            FuelSurchargeScoreCodePrefixRule("X"),
            FuelSurchargeScoreAllowedRegionRule(mutableListOf<String>("legacy")),
            FuelSurchargeScoreCodeLengthRule(255),
            FuelSurchargeScoreNameLengthRule(16),
            FuelSurchargeScoreSourceLengthRule(3600),
        ),
    )
}
