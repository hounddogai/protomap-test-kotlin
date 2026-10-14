package com.acme.transport.safety.violation.violationslot

/** A rule a regional violation slot must pass. Check returns its problems, or none. */
interface RegionalViolationSlotRule {
    fun check(item: RegionalViolationSlot): MutableList<String>
}

data class RegionalViolationSlotRevisionRangeRule(var min: Int, var max: Int) : RegionalViolationSlotRule {
    override fun check(item: RegionalViolationSlot): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class RegionalViolationSlotRequiredChannelRule(var message: String) : RegionalViolationSlotRule {
    override fun check(item: RegionalViolationSlot): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationSlotChannelLengthRule(var maxLength: Int) : RegionalViolationSlotRule {
    override fun check(item: RegionalViolationSlot): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationSlotRequiredCodeRule(var message: String) : RegionalViolationSlotRule {
    override fun check(item: RegionalViolationSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationSlotRequiredLabelRule(var message: String) : RegionalViolationSlotRule {
    override fun check(item: RegionalViolationSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional violation slot. */
data class RegionalViolationSlotValidator(var rules: MutableList<RegionalViolationSlotRule>) {
    fun validate(item: RegionalViolationSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalViolationSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalViolationSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalViolationSlotValidator(): RegionalViolationSlotValidator {
    return RegionalViolationSlotValidator(
        mutableListOf<RegionalViolationSlotRule>(
            RegionalViolationSlotRevisionRangeRule(120, 255),
            RegionalViolationSlotRequiredChannelRule("regional violation slot needs an entry"),
            RegionalViolationSlotChannelLengthRule(255),
            RegionalViolationSlotRequiredCodeRule("regional violation slot needs a value"),
            RegionalViolationSlotRequiredLabelRule("regional violation slot needs an entry"),
        ),
    )
}
