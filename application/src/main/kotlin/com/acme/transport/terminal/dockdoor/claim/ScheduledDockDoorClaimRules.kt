package com.acme.transport.terminal.dockdoor.claim

/** A rule a scheduled dock door claim must pass. Check returns its problems, or none. */
interface ScheduledDockDoorClaimRule {
    fun check(item: ScheduledDockDoorClaim): MutableList<String>
}

data class ScheduledDockDoorClaimCodePrefixRule(var prefix: String) : ScheduledDockDoorClaimRule {
    override fun check(item: ScheduledDockDoorClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorClaimBudgetRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledDockDoorClaimRule {
    override fun check(item: ScheduledDockDoorClaim): MutableList<String> {
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

data class ScheduledDockDoorClaimPriorityRangeRule(var min: Int, var max: Int) : ScheduledDockDoorClaimRule {
    override fun check(item: ScheduledDockDoorClaim): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class ScheduledDockDoorClaimCodeLengthRule(var maxLength: Int) : ScheduledDockDoorClaimRule {
    override fun check(item: ScheduledDockDoorClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorClaimRequiredCodeRule(var message: String) : ScheduledDockDoorClaimRule {
    override fun check(item: ScheduledDockDoorClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dock door claim. */
data class ScheduledDockDoorClaimValidator(var rules: MutableList<ScheduledDockDoorClaimRule>) {
    fun validate(item: ScheduledDockDoorClaim): MutableList<String> {
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

    fun requireValid(item: ScheduledDockDoorClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDockDoorClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDockDoorClaimValidator(): ScheduledDockDoorClaimValidator {
    return ScheduledDockDoorClaimValidator(
        mutableListOf<ScheduledDockDoorClaimRule>(
            ScheduledDockDoorClaimCodePrefixRule("ACM"),
            ScheduledDockDoorClaimBudgetRangeRule(7.0, 4880.4),
            ScheduledDockDoorClaimPriorityRangeRule(64, 3600),
            ScheduledDockDoorClaimCodeLengthRule(1000),
            ScheduledDockDoorClaimRequiredCodeRule("scheduled dock door claim needs an entry"),
        ),
    )
}
