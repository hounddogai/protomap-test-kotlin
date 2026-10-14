package com.acme.transport.safety.nearmiss.proposal

/** A rule a pending near miss slot must pass. Check returns its problems, or none. */
interface PendingNearMissSlotRule {
    fun check(item: PendingNearMissSlot): MutableList<String>
}

data class PendingNearMissSlotBudgetRangeRule(var min: Double, var max: Double) : PendingNearMissSlotRule {
    override fun check(item: PendingNearMissSlot): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissSlotUnitLengthRule(var maxLength: Int) : PendingNearMissSlotRule {
    override fun check(item: PendingNearMissSlot): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissSlotNameLengthRule(var maxLength: Int) : PendingNearMissSlotRule {
    override fun check(item: PendingNearMissSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissSlotDockLengthRule(var maxLength: Int) : PendingNearMissSlotRule {
    override fun check(item: PendingNearMissSlot): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending near miss slot. */
data class PendingNearMissSlotValidator(var rules: MutableList<PendingNearMissSlotRule>) {
    fun validate(item: PendingNearMissSlot): MutableList<String> {
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

    fun requireValid(item: PendingNearMissSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNearMissSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNearMissSlotValidator(): PendingNearMissSlotValidator {
    return PendingNearMissSlotValidator(
        mutableListOf<PendingNearMissSlotRule>(
            PendingNearMissSlotBudgetRangeRule(9.0, 821.9),
            PendingNearMissSlotUnitLengthRule(120),
            PendingNearMissSlotNameLengthRule(255),
            PendingNearMissSlotDockLengthRule(1000),
        ),
    )
}
