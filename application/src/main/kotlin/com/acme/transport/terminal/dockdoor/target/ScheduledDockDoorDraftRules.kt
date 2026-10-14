package com.acme.transport.terminal.dockdoor.target

/** A rule a scheduled dock door draft must pass. Check returns its problems, or none. */
interface ScheduledDockDoorDraftRule {
    fun check(item: ScheduledDockDoorDraft): MutableList<String>
}

data class ScheduledDockDoorDraftCodeLengthRule(var maxLength: Int) : ScheduledDockDoorDraftRule {
    override fun check(item: ScheduledDockDoorDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorDraftRequiredCodeRule(var message: String) : ScheduledDockDoorDraftRule {
    override fun check(item: ScheduledDockDoorDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorDraftCodePrefixRule(var prefix: String) : ScheduledDockDoorDraftRule {
    override fun check(item: ScheduledDockDoorDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorDraftOdometerRangeRule(var min: Int, var max: Int) : ScheduledDockDoorDraftRule {
    override fun check(item: ScheduledDockDoorDraft): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class ScheduledDockDoorDraftRequiredTitleRule(var message: String) : ScheduledDockDoorDraftRule {
    override fun check(item: ScheduledDockDoorDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dock door draft. */
data class ScheduledDockDoorDraftValidator(var rules: MutableList<ScheduledDockDoorDraftRule>) {
    fun validate(item: ScheduledDockDoorDraft): MutableList<String> {
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

    fun requireValid(item: ScheduledDockDoorDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDockDoorDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDockDoorDraftValidator(): ScheduledDockDoorDraftValidator {
    return ScheduledDockDoorDraftValidator(
        mutableListOf<ScheduledDockDoorDraftRule>(
            ScheduledDockDoorDraftCodeLengthRule(16),
            ScheduledDockDoorDraftRequiredCodeRule("scheduled dock door draft needs this field"),
            ScheduledDockDoorDraftCodePrefixRule("Q"),
            ScheduledDockDoorDraftOdometerRangeRule(32, 64),
            ScheduledDockDoorDraftRequiredTitleRule("scheduled dock door draft needs an entry"),
        ),
    )
}
