package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge exception must pass. Check returns its problems, or none. */
interface FuelSurchargeExceptionRule {
    fun check(item: FuelSurchargeException): MutableList<String>
}

data class FuelSurchargeExceptionOwnerIdLengthRule(var maxLength: Int) : FuelSurchargeExceptionRule {
    override fun check(item: FuelSurchargeException): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeExceptionCodeLengthRule(var maxLength: Int) : FuelSurchargeExceptionRule {
    override fun check(item: FuelSurchargeException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeExceptionAllowedTitleRule(
    var allowed: MutableList<String>,
) : FuelSurchargeExceptionRule {
    override fun check(item: FuelSurchargeException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge exception. */
data class FuelSurchargeExceptionValidator(var rules: MutableList<FuelSurchargeExceptionRule>) {
    fun validate(item: FuelSurchargeException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeException>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeExceptionValidator(): FuelSurchargeExceptionValidator {
    return FuelSurchargeExceptionValidator(
        mutableListOf<FuelSurchargeExceptionRule>(
            FuelSurchargeExceptionOwnerIdLengthRule(16),
            FuelSurchargeExceptionCodeLengthRule(86400),
            FuelSurchargeExceptionAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
