package com.acme.transport.safety.violation.slot

import com.acme.transport.kit.text.normalize

/** A rule a violation slot must pass. Check returns its problems, or none. */
interface ViolationSlotRule {
    fun check(item: ViolationSlot): MutableList<String>
}

data class ViolationSlotAllowedCodeRule(var allowed: MutableList<String>) : ViolationSlotRule {
    override fun check(item: ViolationSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ViolationSlotRequiredTrailerIdRule(var message: String) : ViolationSlotRule {
    override fun check(item: ViolationSlot): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationSlotStopCountRangeRule(var min: Int, var max: Int) : ViolationSlotRule {
    override fun check(item: ViolationSlot): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation slot. */
data class ViolationSlotValidator(var rules: MutableList<ViolationSlotRule>) {
    fun validate(item: ViolationSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationSlotValidator(): ViolationSlotValidator {
    return ViolationSlotValidator(
        mutableListOf<ViolationSlotRule>(
            ViolationSlotAllowedCodeRule(mutableListOf<String>("batched")),
            ViolationSlotRequiredTrailerIdRule("violation slot needs an entry"),
            ViolationSlotStopCountRangeRule(16, 80),
        ),
    )
}
