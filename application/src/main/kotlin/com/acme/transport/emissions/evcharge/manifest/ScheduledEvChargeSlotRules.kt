package com.acme.transport.emissions.evcharge.manifest

/** A rule a scheduled ev charge slot must pass. Check returns its problems, or none. */
interface ScheduledEvChargeSlotRule {
    fun check(item: ScheduledEvChargeSlot): MutableList<String>
}

data class ScheduledEvChargeSlotExternalRefLengthRule(var maxLength: Int) : ScheduledEvChargeSlotRule {
    override fun check(item: ScheduledEvChargeSlot): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEvChargeSlotRequiredCodeRule(var message: String) : ScheduledEvChargeSlotRule {
    override fun check(item: ScheduledEvChargeSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEvChargeSlotCodeLengthRule(var maxLength: Int) : ScheduledEvChargeSlotRule {
    override fun check(item: ScheduledEvChargeSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEvChargeSlotScoreRangeRule(var min: Double, var max: Double) : ScheduledEvChargeSlotRule {
    override fun check(item: ScheduledEvChargeSlot): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled ev charge slot. */
data class ScheduledEvChargeSlotValidator(var rules: MutableList<ScheduledEvChargeSlotRule>) {
    fun validate(item: ScheduledEvChargeSlot): MutableList<String> {
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

    fun requireValid(item: ScheduledEvChargeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledEvChargeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledEvChargeSlotValidator(): ScheduledEvChargeSlotValidator {
    return ScheduledEvChargeSlotValidator(
        mutableListOf<ScheduledEvChargeSlotRule>(
            ScheduledEvChargeSlotExternalRefLengthRule(255),
            ScheduledEvChargeSlotRequiredCodeRule("scheduled ev charge slot needs this field"),
            ScheduledEvChargeSlotCodeLengthRule(3600),
            ScheduledEvChargeSlotScoreRangeRule(2.4, 3095.9),
        ),
    )
}
