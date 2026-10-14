package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge group must pass. Check returns its problems, or none. */
interface FuelSurchargeGroupRule {
    fun check(item: FuelSurchargeGroup): MutableList<String>
}

data class FuelSurchargeGroupCodePrefixRule(var prefix: String) : FuelSurchargeGroupRule {
    override fun check(item: FuelSurchargeGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeGroupRequiredContactEmailRule(var message: String) : FuelSurchargeGroupRule {
    override fun check(item: FuelSurchargeGroup): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeGroupCodeLengthRule(var maxLength: Int) : FuelSurchargeGroupRule {
    override fun check(item: FuelSurchargeGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeGroupAllowedCodeRule(var allowed: MutableList<String>) : FuelSurchargeGroupRule {
    override fun check(item: FuelSurchargeGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeGroupRequiredTitleRule(var message: String) : FuelSurchargeGroupRule {
    override fun check(item: FuelSurchargeGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge group. */
data class FuelSurchargeGroupValidator(var rules: MutableList<FuelSurchargeGroupRule>) {
    fun validate(item: FuelSurchargeGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeGroupValidator(): FuelSurchargeGroupValidator {
    return FuelSurchargeGroupValidator(
        mutableListOf<FuelSurchargeGroupRule>(
            FuelSurchargeGroupCodePrefixRule("X"),
            FuelSurchargeGroupRequiredContactEmailRule("fuel surcharge group needs an entry"),
            FuelSurchargeGroupCodeLengthRule(255),
            FuelSurchargeGroupAllowedCodeRule(mutableListOf<String>("rolling", "manual", "primary")),
            FuelSurchargeGroupRequiredTitleRule("fuel surcharge group needs a value"),
        ),
    )
}
