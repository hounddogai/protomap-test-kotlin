package com.acme.transport.terminal.crossdock.slot

/** A rule a cross dock slot must pass. Check returns its problems, or none. */
interface CrossDockSlotRule {
    fun check(item: CrossDockSlot): MutableList<String>
}

data class CrossDockSlotRequiredLabelRule(var message: String) : CrossDockSlotRule {
    override fun check(item: CrossDockSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockSlotPriorityRangeRule(var min: Int, var max: Int) : CrossDockSlotRule {
    override fun check(item: CrossDockSlot): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockSlotCodeLengthRule(var maxLength: Int) : CrossDockSlotRule {
    override fun check(item: CrossDockSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockSlotCodePrefixRule(var prefix: String) : CrossDockSlotRule {
    override fun check(item: CrossDockSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock slot. */
data class CrossDockSlotValidator(var rules: MutableList<CrossDockSlotRule>) {
    fun validate(item: CrossDockSlot): MutableList<String> {
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

    fun requireValid(item: CrossDockSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockSlotValidator(): CrossDockSlotValidator {
    return CrossDockSlotValidator(
        mutableListOf<CrossDockSlotRule>(
            CrossDockSlotRequiredLabelRule("cross dock slot needs an entry"),
            CrossDockSlotPriorityRangeRule(16, 120),
            CrossDockSlotCodeLengthRule(1000),
            CrossDockSlotCodePrefixRule("REF"),
        ),
    )
}
