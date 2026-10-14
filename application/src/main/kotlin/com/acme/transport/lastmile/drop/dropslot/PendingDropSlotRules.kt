package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.kit.text.normalize

/** A rule a pending drop slot must pass. Check returns its problems, or none. */
interface PendingDropSlotRule {
    fun check(item: PendingDropSlot): MutableList<String>
}

data class PendingDropSlotShiftCodeLengthRule(var maxLength: Int) : PendingDropSlotRule {
    override fun check(item: PendingDropSlot): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDropSlotOdometerRangeRule(var min: Int, var max: Int) : PendingDropSlotRule {
    override fun check(item: PendingDropSlot): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDropSlotRequiredOwnerIdRule(var message: String) : PendingDropSlotRule {
    override fun check(item: PendingDropSlot): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDropSlotAllowedShiftCodeRule(var allowed: MutableList<String>) : PendingDropSlotRule {
    override fun check(item: PendingDropSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending drop slot. */
data class PendingDropSlotValidator(var rules: MutableList<PendingDropSlotRule>) {
    fun validate(item: PendingDropSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDropSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDropSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDropSlotValidator(): PendingDropSlotValidator {
    return PendingDropSlotValidator(
        mutableListOf<PendingDropSlotRule>(
            PendingDropSlotShiftCodeLengthRule(3600),
            PendingDropSlotOdometerRangeRule(32, 80),
            PendingDropSlotRequiredOwnerIdRule("pending drop slot needs an entry"),
            PendingDropSlotAllowedShiftCodeRule(mutableListOf<String>("legacy", "adaptive")),
        ),
    )
}
