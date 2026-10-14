package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.text.normalize

/** A rule a dwell slot must pass. Check returns its problems, or none. */
interface DwellSlotRule {
    fun check(item: DwellSlot): MutableList<String>
}

data class DwellSlotAllowedLaneRule(var allowed: MutableList<String>) : DwellSlotRule {
    override fun check(item: DwellSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class DwellSlotSourceLengthRule(var maxLength: Int) : DwellSlotRule {
    override fun check(item: DwellSlot): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellSlotRequiredCodeRule(var message: String) : DwellSlotRule {
    override fun check(item: DwellSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellSlotRequiredSourceRule(var message: String) : DwellSlotRule {
    override fun check(item: DwellSlot): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellSlotRequiredNameRule(var message: String) : DwellSlotRule {
    override fun check(item: DwellSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellSlotCodeLengthRule(var maxLength: Int) : DwellSlotRule {
    override fun check(item: DwellSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell slot. */
data class DwellSlotValidator(var rules: MutableList<DwellSlotRule>) {
    fun validate(item: DwellSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellSlotValidator(): DwellSlotValidator {
    return DwellSlotValidator(
        mutableListOf<DwellSlotRule>(
            DwellSlotAllowedLaneRule(mutableListOf<String>("scheduled")),
            DwellSlotSourceLengthRule(64),
            DwellSlotRequiredCodeRule("dwell slot needs this field"),
            DwellSlotRequiredSourceRule("dwell slot needs a value"),
            DwellSlotRequiredNameRule("dwell slot needs this field"),
            DwellSlotCodeLengthRule(32),
        ),
    )
}
