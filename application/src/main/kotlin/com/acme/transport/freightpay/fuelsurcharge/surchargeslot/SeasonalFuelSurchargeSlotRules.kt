package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.kit.text.normalize

/** A rule a seasonal fuel surcharge slot must pass. Check returns its problems, or none. */
interface SeasonalFuelSurchargeSlotRule {
    fun check(item: SeasonalFuelSurchargeSlot): MutableList<String>
}

data class SeasonalFuelSurchargeSlotCodePrefixRule(var prefix: String) : SeasonalFuelSurchargeSlotRule {
    override fun check(item: SeasonalFuelSurchargeSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeSlotAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalFuelSurchargeSlotRule {
    override fun check(item: SeasonalFuelSurchargeSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeSlotRequiredCodeRule(var message: String) : SeasonalFuelSurchargeSlotRule {
    override fun check(item: SeasonalFuelSurchargeSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeSlotCodeLengthRule(var maxLength: Int) : SeasonalFuelSurchargeSlotRule {
    override fun check(item: SeasonalFuelSurchargeSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal fuel surcharge slot. */
data class SeasonalFuelSurchargeSlotValidator(var rules: MutableList<SeasonalFuelSurchargeSlotRule>) {
    fun validate(item: SeasonalFuelSurchargeSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalFuelSurchargeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalFuelSurchargeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalFuelSurchargeSlotValidator(): SeasonalFuelSurchargeSlotValidator {
    return SeasonalFuelSurchargeSlotValidator(
        mutableListOf<SeasonalFuelSurchargeSlotRule>(
            SeasonalFuelSurchargeSlotCodePrefixRule("ACM"),
            SeasonalFuelSurchargeSlotAllowedCodeRule(mutableListOf<String>("capped", "rolling")),
            SeasonalFuelSurchargeSlotRequiredCodeRule("seasonal fuel surcharge slot needs this field"),
            SeasonalFuelSurchargeSlotCodeLengthRule(64),
        ),
    )
}
