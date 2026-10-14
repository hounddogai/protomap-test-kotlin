package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.text.normalize

/** A rule a lenient dock door slot must pass. Check returns its problems, or none. */
interface LenientDockDoorSlotRule {
    fun check(item: LenientDockDoorSlot): MutableList<String>
}

data class LenientDockDoorSlotAllowedLabelRule(var allowed: MutableList<String>) : LenientDockDoorSlotRule {
    override fun check(item: LenientDockDoorSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class LenientDockDoorSlotRequiredCodeRule(var message: String) : LenientDockDoorSlotRule {
    override fun check(item: LenientDockDoorSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDockDoorSlotWeightRangeRule(var min: Double, var max: Double) : LenientDockDoorSlotRule {
    override fun check(item: LenientDockDoorSlot): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient dock door slot. */
data class LenientDockDoorSlotValidator(var rules: MutableList<LenientDockDoorSlotRule>) {
    fun validate(item: LenientDockDoorSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDockDoorSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDockDoorSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDockDoorSlotValidator(): LenientDockDoorSlotValidator {
    return LenientDockDoorSlotValidator(
        mutableListOf<LenientDockDoorSlotRule>(
            LenientDockDoorSlotAllowedLabelRule(mutableListOf<String>("adaptive")),
            LenientDockDoorSlotRequiredCodeRule("lenient dock door slot needs an entry"),
            LenientDockDoorSlotWeightRangeRule(0.7, 1056.1),
        ),
    )
}
