package com.acme.transport.terminal.crossdock.dockslot

/** A rule a scheduled cross dock slot must pass. Check returns its problems, or none. */
interface ScheduledCrossDockSlotRule {
    fun check(item: ScheduledCrossDockSlot): MutableList<String>
}

data class ScheduledCrossDockSlotCodePrefixRule(var prefix: String) : ScheduledCrossDockSlotRule {
    override fun check(item: ScheduledCrossDockSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCrossDockSlotNameLengthRule(var maxLength: Int) : ScheduledCrossDockSlotRule {
    override fun check(item: ScheduledCrossDockSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCrossDockSlotRequiredNameRule(var message: String) : ScheduledCrossDockSlotRule {
    override fun check(item: ScheduledCrossDockSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled cross dock slot. */
data class ScheduledCrossDockSlotValidator(var rules: MutableList<ScheduledCrossDockSlotRule>) {
    fun validate(item: ScheduledCrossDockSlot): MutableList<String> {
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

    fun requireValid(item: ScheduledCrossDockSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCrossDockSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCrossDockSlotValidator(): ScheduledCrossDockSlotValidator {
    return ScheduledCrossDockSlotValidator(
        mutableListOf<ScheduledCrossDockSlotRule>(
            ScheduledCrossDockSlotCodePrefixRule("OPS"),
            ScheduledCrossDockSlotNameLengthRule(3600),
            ScheduledCrossDockSlotRequiredNameRule("scheduled cross dock slot needs an entry"),
        ),
    )
}
