package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.kit.text.normalize

/** A rule a regional fuel surcharge adjustment must pass. Check returns its problems, or none. */
interface RegionalFuelSurchargeAdjustmentRule {
    fun check(item: RegionalFuelSurchargeAdjustment): MutableList<String>
}

data class RegionalFuelSurchargeAdjustmentRequiredCodeRule(
    var message: String,
) : RegionalFuelSurchargeAdjustmentRule {
    override fun check(item: RegionalFuelSurchargeAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeAdjustmentAllowedNameRule(
    var allowed: MutableList<String>,
) : RegionalFuelSurchargeAdjustmentRule {
    override fun check(item: RegionalFuelSurchargeAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeAdjustmentRequiredDepotRule(
    var message: String,
) : RegionalFuelSurchargeAdjustmentRule {
    override fun check(item: RegionalFuelSurchargeAdjustment): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeAdjustmentCodeLengthRule(
    var maxLength: Int,
) : RegionalFuelSurchargeAdjustmentRule {
    override fun check(item: RegionalFuelSurchargeAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeAdjustmentLabelLengthRule(
    var maxLength: Int,
) : RegionalFuelSurchargeAdjustmentRule {
    override fun check(item: RegionalFuelSurchargeAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeAdjustmentDepotLengthRule(
    var maxLength: Int,
) : RegionalFuelSurchargeAdjustmentRule {
    override fun check(item: RegionalFuelSurchargeAdjustment): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional fuel surcharge adjustment. */
data class RegionalFuelSurchargeAdjustmentValidator(
    var rules: MutableList<RegionalFuelSurchargeAdjustmentRule>,
) {
    fun validate(item: RegionalFuelSurchargeAdjustment): MutableList<String> {
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

    fun requireValid(item: RegionalFuelSurchargeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelSurchargeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelSurchargeAdjustmentValidator(): RegionalFuelSurchargeAdjustmentValidator {
    return RegionalFuelSurchargeAdjustmentValidator(
        mutableListOf<RegionalFuelSurchargeAdjustmentRule>(
            RegionalFuelSurchargeAdjustmentRequiredCodeRule("regional fuel surcharge adjustment needs an entry"),
            RegionalFuelSurchargeAdjustmentAllowedNameRule(mutableListOf<String>()),
            RegionalFuelSurchargeAdjustmentRequiredDepotRule(
                "regional fuel surcharge adjustment needs an entry",
            ),
            RegionalFuelSurchargeAdjustmentCodeLengthRule(64),
            RegionalFuelSurchargeAdjustmentLabelLengthRule(3600),
            RegionalFuelSurchargeAdjustmentDepotLengthRule(255),
        ),
    )
}
