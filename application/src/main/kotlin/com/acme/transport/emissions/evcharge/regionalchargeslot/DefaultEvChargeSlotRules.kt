package com.acme.transport.emissions.evcharge.regionalchargeslot

/** A rule a default ev charge slot must pass. Check returns its problems, or none. */
interface DefaultEvChargeSlotRule {
    fun check(item: DefaultEvChargeSlot): MutableList<String>
}

data class DefaultEvChargeSlotCostRangeRule(var min: Double, var max: Double) : DefaultEvChargeSlotRule {
    override fun check(item: DefaultEvChargeSlot): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class DefaultEvChargeSlotVersionRangeRule(var min: Int, var max: Int) : DefaultEvChargeSlotRule {
    override fun check(item: DefaultEvChargeSlot): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultEvChargeSlotCodeLengthRule(var maxLength: Int) : DefaultEvChargeSlotRule {
    override fun check(item: DefaultEvChargeSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default ev charge slot. */
data class DefaultEvChargeSlotValidator(var rules: MutableList<DefaultEvChargeSlotRule>) {
    fun validate(item: DefaultEvChargeSlot): MutableList<String> {
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

    fun requireValid(item: DefaultEvChargeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultEvChargeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultEvChargeSlotValidator(): DefaultEvChargeSlotValidator {
    return DefaultEvChargeSlotValidator(
        mutableListOf<DefaultEvChargeSlotRule>(
            DefaultEvChargeSlotCostRangeRule(1.8, 837.3),
            DefaultEvChargeSlotVersionRangeRule(32, 120),
            DefaultEvChargeSlotCodeLengthRule(80),
        ),
    )
}
