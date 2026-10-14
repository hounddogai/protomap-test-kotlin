package com.acme.transport.freightpay.fuelsurcharge.slot

/** A rule a fuel surcharge slot must pass. Check returns its problems, or none. */
interface FuelSurchargeSlotRule {
    fun check(item: FuelSurchargeSlot): MutableList<String>
}

data class FuelSurchargeSlotStopCountRangeRule(var min: Int, var max: Int) : FuelSurchargeSlotRule {
    override fun check(item: FuelSurchargeSlot): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSlotRequiredCodeRule(var message: String) : FuelSurchargeSlotRule {
    override fun check(item: FuelSurchargeSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSlotCodePrefixRule(var prefix: String) : FuelSurchargeSlotRule {
    override fun check(item: FuelSurchargeSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSlotTitleLengthRule(var maxLength: Int) : FuelSurchargeSlotRule {
    override fun check(item: FuelSurchargeSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge slot. */
data class FuelSurchargeSlotValidator(var rules: MutableList<FuelSurchargeSlotRule>) {
    fun validate(item: FuelSurchargeSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeSlotValidator(): FuelSurchargeSlotValidator {
    return FuelSurchargeSlotValidator(
        mutableListOf<FuelSurchargeSlotRule>(
            FuelSurchargeSlotStopCountRangeRule(64, 86400),
            FuelSurchargeSlotRequiredCodeRule("fuel surcharge slot needs this field"),
            FuelSurchargeSlotCodePrefixRule("REF"),
            FuelSurchargeSlotTitleLengthRule(16),
        ),
    )
}
