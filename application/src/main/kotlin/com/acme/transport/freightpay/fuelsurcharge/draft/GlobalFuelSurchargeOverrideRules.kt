package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.kit.text.normalize

/** A rule a global fuel surcharge override must pass. Check returns its problems, or none. */
interface GlobalFuelSurchargeOverrideRule {
    fun check(item: GlobalFuelSurchargeOverride): MutableList<String>
}

data class GlobalFuelSurchargeOverrideRequiredOwnerIdRule(
    var message: String,
) : GlobalFuelSurchargeOverrideRule {
    override fun check(item: GlobalFuelSurchargeOverride): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeOverrideRequiredLabelRule(var message: String) : GlobalFuelSurchargeOverrideRule {
    override fun check(item: GlobalFuelSurchargeOverride): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeOverrideDiscountRangeRule(
    var min: Double,
    var max: Double,
) : GlobalFuelSurchargeOverrideRule {
    override fun check(item: GlobalFuelSurchargeOverride): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeOverrideAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : GlobalFuelSurchargeOverrideRule {
    override fun check(item: GlobalFuelSurchargeOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeOverrideCodePrefixRule(var prefix: String) : GlobalFuelSurchargeOverrideRule {
    override fun check(item: GlobalFuelSurchargeOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFuelSurchargeOverrideCodeLengthRule(var maxLength: Int) : GlobalFuelSurchargeOverrideRule {
    override fun check(item: GlobalFuelSurchargeOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global fuel surcharge override. */
data class GlobalFuelSurchargeOverrideValidator(var rules: MutableList<GlobalFuelSurchargeOverrideRule>) {
    fun validate(item: GlobalFuelSurchargeOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalFuelSurchargeOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFuelSurchargeOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFuelSurchargeOverrideValidator(): GlobalFuelSurchargeOverrideValidator {
    return GlobalFuelSurchargeOverrideValidator(
        mutableListOf<GlobalFuelSurchargeOverrideRule>(
            GlobalFuelSurchargeOverrideRequiredOwnerIdRule("global fuel surcharge override needs an entry"),
            GlobalFuelSurchargeOverrideRequiredLabelRule("global fuel surcharge override needs this field"),
            GlobalFuelSurchargeOverrideDiscountRangeRule(2.5, 73.7),
            GlobalFuelSurchargeOverrideAllowedRouteCodeRule(mutableListOf<String>("regional", "legacy")),
            GlobalFuelSurchargeOverrideCodePrefixRule("REF"),
            GlobalFuelSurchargeOverrideCodeLengthRule(255),
        ),
    )
}
