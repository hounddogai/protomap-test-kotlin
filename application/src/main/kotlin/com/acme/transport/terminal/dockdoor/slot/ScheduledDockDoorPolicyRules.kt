package com.acme.transport.terminal.dockdoor.slot

/** A rule a scheduled dock door policy must pass. Check returns its problems, or none. */
interface ScheduledDockDoorPolicyRule {
    fun check(item: ScheduledDockDoorPolicy): MutableList<String>
}

data class ScheduledDockDoorPolicyRequiredCodeRule(var message: String) : ScheduledDockDoorPolicyRule {
    override fun check(item: ScheduledDockDoorPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorPolicyCodePrefixRule(var prefix: String) : ScheduledDockDoorPolicyRule {
    override fun check(item: ScheduledDockDoorPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorPolicyRequiredTitleRule(var message: String) : ScheduledDockDoorPolicyRule {
    override fun check(item: ScheduledDockDoorPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dock door policy. */
data class ScheduledDockDoorPolicyValidator(var rules: MutableList<ScheduledDockDoorPolicyRule>) {
    fun validate(item: ScheduledDockDoorPolicy): MutableList<String> {
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

    fun requireValid(item: ScheduledDockDoorPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDockDoorPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDockDoorPolicyValidator(): ScheduledDockDoorPolicyValidator {
    return ScheduledDockDoorPolicyValidator(
        mutableListOf<ScheduledDockDoorPolicyRule>(
            ScheduledDockDoorPolicyRequiredCodeRule("scheduled dock door policy needs this field"),
            ScheduledDockDoorPolicyCodePrefixRule("REF"),
            ScheduledDockDoorPolicyRequiredTitleRule("scheduled dock door policy needs an entry"),
        ),
    )
}
