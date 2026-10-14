package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge request must pass. Check returns its problems, or none. */
interface FuelSurchargeRequestRule {
    fun check(item: FuelSurchargeRequest): MutableList<String>
}

data class FuelSurchargeRequestLabelLengthRule(var maxLength: Int) : FuelSurchargeRequestRule {
    override fun check(item: FuelSurchargeRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRequestCodePrefixRule(var prefix: String) : FuelSurchargeRequestRule {
    override fun check(item: FuelSurchargeRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRequestAllowedLabelRule(var allowed: MutableList<String>) : FuelSurchargeRequestRule {
    override fun check(item: FuelSurchargeRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRequestRequiredCodeRule(var message: String) : FuelSurchargeRequestRule {
    override fun check(item: FuelSurchargeRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRequestEndsAtRangeRule(var min: Int, var max: Int) : FuelSurchargeRequestRule {
    override fun check(item: FuelSurchargeRequest): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fuel surcharge request. */
data class FuelSurchargeRequestValidator(var rules: MutableList<FuelSurchargeRequestRule>) {
    fun validate(item: FuelSurchargeRequest): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeRequestValidator(): FuelSurchargeRequestValidator {
    return FuelSurchargeRequestValidator(
        mutableListOf<FuelSurchargeRequestRule>(
            FuelSurchargeRequestLabelLengthRule(86400),
            FuelSurchargeRequestCodePrefixRule("REF"),
            FuelSurchargeRequestAllowedLabelRule(mutableListOf<String>("weighted")),
            FuelSurchargeRequestRequiredCodeRule("fuel surcharge request needs an entry"),
            FuelSurchargeRequestEndsAtRangeRule(80, 120),
        ),
    )
}
