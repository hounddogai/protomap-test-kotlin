package com.acme.transport.crewing.availability.slot

import com.acme.transport.kit.text.normalize

/** A rule a availability slot must pass. Check returns its problems, or none. */
interface AvailabilitySlotRule {
    fun check(item: AvailabilitySlot): MutableList<String>
}

data class AvailabilitySlotBudgetRangeRule(var min: Double, var max: Double) : AvailabilitySlotRule {
    override fun check(item: AvailabilitySlot): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class AvailabilitySlotAllowedDockRule(var allowed: MutableList<String>) : AvailabilitySlotRule {
    override fun check(item: AvailabilitySlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySlotRequiredCarrierRule(var message: String) : AvailabilitySlotRule {
    override fun check(item: AvailabilitySlot): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySlotRequiredDockRule(var message: String) : AvailabilitySlotRule {
    override fun check(item: AvailabilitySlot): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySlotRetriesRangeRule(var min: Int, var max: Int) : AvailabilitySlotRule {
    override fun check(item: AvailabilitySlot): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySlotRequiredLabelRule(var message: String) : AvailabilitySlotRule {
    override fun check(item: AvailabilitySlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability slot. */
data class AvailabilitySlotValidator(var rules: MutableList<AvailabilitySlotRule>) {
    fun validate(item: AvailabilitySlot): MutableList<String> {
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

    fun requireValid(item: AvailabilitySlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilitySlot>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilitySlotValidator(): AvailabilitySlotValidator {
    return AvailabilitySlotValidator(
        mutableListOf<AvailabilitySlotRule>(
            AvailabilitySlotBudgetRangeRule(9.5, 2150.0),
            AvailabilitySlotAllowedDockRule(mutableListOf<String>("rolling", "strict")),
            AvailabilitySlotRequiredCarrierRule("availability slot needs an entry"),
            AvailabilitySlotRequiredDockRule("availability slot needs this field"),
            AvailabilitySlotRetriesRangeRule(64, 86400),
            AvailabilitySlotRequiredLabelRule("availability slot needs an entry"),
        ),
    )
}
