package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.text.normalize

/** A rule a dock door slot must pass. Check returns its problems, or none. */
interface DockDoorSlotRule {
    fun check(item: DockDoorSlot): MutableList<String>
}

data class DockDoorSlotEndsAtRangeRule(var min: Int, var max: Int) : DockDoorSlotRule {
    override fun check(item: DockDoorSlot): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorSlotRequiredCodeRule(var message: String) : DockDoorSlotRule {
    override fun check(item: DockDoorSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorSlotCodePrefixRule(var prefix: String) : DockDoorSlotRule {
    override fun check(item: DockDoorSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorSlotAllowedCodeRule(var allowed: MutableList<String>) : DockDoorSlotRule {
    override fun check(item: DockDoorSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorSlotRequiredLocaleRule(var message: String) : DockDoorSlotRule {
    override fun check(item: DockDoorSlot): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door slot. */
data class DockDoorSlotValidator(var rules: MutableList<DockDoorSlotRule>) {
    fun validate(item: DockDoorSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorSlotValidator(): DockDoorSlotValidator {
    return DockDoorSlotValidator(
        mutableListOf<DockDoorSlotRule>(
            DockDoorSlotEndsAtRangeRule(16, 80),
            DockDoorSlotRequiredCodeRule("dock door slot needs this field"),
            DockDoorSlotCodePrefixRule("OPS"),
            DockDoorSlotAllowedCodeRule(mutableListOf<String>("legacy")),
            DockDoorSlotRequiredLocaleRule("dock door slot needs a value"),
        ),
    )
}
