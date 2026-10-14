package com.acme.transport.lastmile.drop.slot

/** A rule a drop slot must pass. Check returns its problems, or none. */
interface DropSlotRule {
    fun check(item: DropSlot): MutableList<String>
}

data class DropSlotTitleLengthRule(var maxLength: Int) : DropSlotRule {
    override fun check(item: DropSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropSlotRequiredCodeRule(var message: String) : DropSlotRule {
    override fun check(item: DropSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropSlotDriverIdLengthRule(var maxLength: Int) : DropSlotRule {
    override fun check(item: DropSlot): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropSlotSequenceRangeRule(var min: Int, var max: Int) : DropSlotRule {
    override fun check(item: DropSlot): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop slot. */
data class DropSlotValidator(var rules: MutableList<DropSlotRule>) {
    fun validate(item: DropSlot): MutableList<String> {
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

    fun requireValid(item: DropSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultDropSlotValidator(): DropSlotValidator {
    return DropSlotValidator(
        mutableListOf<DropSlotRule>(
            DropSlotTitleLengthRule(32),
            DropSlotRequiredCodeRule("drop slot needs this field"),
            DropSlotDriverIdLengthRule(1000),
            DropSlotSequenceRangeRule(32, 86400),
        ),
    )
}
