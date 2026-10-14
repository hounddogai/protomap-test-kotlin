package com.acme.transport.customs.clearance.slot

/** A rule a clearance slot must pass. Check returns its problems, or none. */
interface ClearanceSlotRule {
    fun check(item: ClearanceSlot): MutableList<String>
}

data class ClearanceSlotCodeLengthRule(var maxLength: Int) : ClearanceSlotRule {
    override fun check(item: ClearanceSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceSlotCodePrefixRule(var prefix: String) : ClearanceSlotRule {
    override fun check(item: ClearanceSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceSlotCeilingRangeRule(var min: Double, var max: Double) : ClearanceSlotRule {
    override fun check(item: ClearanceSlot): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance slot. */
data class ClearanceSlotValidator(var rules: MutableList<ClearanceSlotRule>) {
    fun validate(item: ClearanceSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceSlotValidator(): ClearanceSlotValidator {
    return ClearanceSlotValidator(
        mutableListOf<ClearanceSlotRule>(
            ClearanceSlotCodeLengthRule(64),
            ClearanceSlotCodePrefixRule("Q"),
            ClearanceSlotCeilingRangeRule(0.6, 657.6),
        ),
    )
}
