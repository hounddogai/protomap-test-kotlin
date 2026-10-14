package com.acme.transport.customs.hold.holdslot

/** A rule a lenient hold slot must pass. Check returns its problems, or none. */
interface LenientHoldSlotRule {
    fun check(item: LenientHoldSlot): MutableList<String>
}

data class LenientHoldSlotCodePrefixRule(var prefix: String) : LenientHoldSlotRule {
    override fun check(item: LenientHoldSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientHoldSlotBudgetRangeRule(var min: Double, var max: Double) : LenientHoldSlotRule {
    override fun check(item: LenientHoldSlot): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientHoldSlotLaneLengthRule(var maxLength: Int) : LenientHoldSlotRule {
    override fun check(item: LenientHoldSlot): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientHoldSlotCodeLengthRule(var maxLength: Int) : LenientHoldSlotRule {
    override fun check(item: LenientHoldSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientHoldSlotRequiredCodeRule(var message: String) : LenientHoldSlotRule {
    override fun check(item: LenientHoldSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientHoldSlotFuelLitersRangeRule(var min: Double, var max: Double) : LenientHoldSlotRule {
    override fun check(item: LenientHoldSlot): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient hold slot. */
data class LenientHoldSlotValidator(var rules: MutableList<LenientHoldSlotRule>) {
    fun validate(item: LenientHoldSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientHoldSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientHoldSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientHoldSlotValidator(): LenientHoldSlotValidator {
    return LenientHoldSlotValidator(
        mutableListOf<LenientHoldSlotRule>(
            LenientHoldSlotCodePrefixRule("OPS"),
            LenientHoldSlotBudgetRangeRule(1.7, 3623.2),
            LenientHoldSlotLaneLengthRule(120),
            LenientHoldSlotCodeLengthRule(1000),
            LenientHoldSlotRequiredCodeRule("lenient hold slot needs this field"),
            LenientHoldSlotFuelLitersRangeRule(0.3, 353.0),
        ),
    )
}
