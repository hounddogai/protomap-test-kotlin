package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.normalize

/** A rule a global availability slot must pass. Check returns its problems, or none. */
interface GlobalAvailabilitySlotRule {
    fun check(item: GlobalAvailabilitySlot): MutableList<String>
}

data class GlobalAvailabilitySlotRequiredLabelRule(var message: String) : GlobalAvailabilitySlotRule {
    override fun check(item: GlobalAvailabilitySlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilitySlotRateRangeRule(var min: Double, var max: Double) : GlobalAvailabilitySlotRule {
    override fun check(item: GlobalAvailabilitySlot): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class GlobalAvailabilitySlotLabelLengthRule(var maxLength: Int) : GlobalAvailabilitySlotRule {
    override fun check(item: GlobalAvailabilitySlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilitySlotCodeLengthRule(var maxLength: Int) : GlobalAvailabilitySlotRule {
    override fun check(item: GlobalAvailabilitySlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilitySlotAllowedDockRule(
    var allowed: MutableList<String>,
) : GlobalAvailabilitySlotRule {
    override fun check(item: GlobalAvailabilitySlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is missing")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilitySlotNameLengthRule(var maxLength: Int) : GlobalAvailabilitySlotRule {
    override fun check(item: GlobalAvailabilitySlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global availability slot. */
data class GlobalAvailabilitySlotValidator(var rules: MutableList<GlobalAvailabilitySlotRule>) {
    fun validate(item: GlobalAvailabilitySlot): MutableList<String> {
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

    fun requireValid(item: GlobalAvailabilitySlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAvailabilitySlot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAvailabilitySlotValidator(): GlobalAvailabilitySlotValidator {
    return GlobalAvailabilitySlotValidator(
        mutableListOf<GlobalAvailabilitySlotRule>(
            GlobalAvailabilitySlotRequiredLabelRule("global availability slot needs this field"),
            GlobalAvailabilitySlotRateRangeRule(3.2, 1641.5),
            GlobalAvailabilitySlotLabelLengthRule(1000),
            GlobalAvailabilitySlotCodeLengthRule(64),
            GlobalAvailabilitySlotAllowedDockRule(mutableListOf<String>("legacy", "default", "capped")),
            GlobalAvailabilitySlotNameLengthRule(120),
        ),
    )
}
