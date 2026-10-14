package com.acme.transport.fleetops.tire.baseline

/** A rule a capped tire slot must pass. Check returns its problems, or none. */
interface CappedTireSlotRule {
    fun check(item: CappedTireSlot): MutableList<String>
}

data class CappedTireSlotCodePrefixRule(var prefix: String) : CappedTireSlotRule {
    override fun check(item: CappedTireSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedTireSlotLabelLengthRule(var maxLength: Int) : CappedTireSlotRule {
    override fun check(item: CappedTireSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTireSlotRequiredCodeRule(var message: String) : CappedTireSlotRule {
    override fun check(item: CappedTireSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTireSlotCostRangeRule(var min: Double, var max: Double) : CappedTireSlotRule {
    override fun check(item: CappedTireSlot): MutableList<String> {
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

/** Runs every rule of a capped tire slot. */
data class CappedTireSlotValidator(var rules: MutableList<CappedTireSlotRule>) {
    fun validate(item: CappedTireSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedTireSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTireSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTireSlotValidator(): CappedTireSlotValidator {
    return CappedTireSlotValidator(
        mutableListOf<CappedTireSlotRule>(
            CappedTireSlotCodePrefixRule("OPS"),
            CappedTireSlotLabelLengthRule(80),
            CappedTireSlotRequiredCodeRule("capped tire slot needs an entry"),
            CappedTireSlotCostRangeRule(0.1, 4192.4),
        ),
    )
}
