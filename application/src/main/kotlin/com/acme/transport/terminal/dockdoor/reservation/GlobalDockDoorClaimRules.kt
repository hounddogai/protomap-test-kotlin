package com.acme.transport.terminal.dockdoor.reservation

/** A rule a global dock door claim must pass. Check returns its problems, or none. */
interface GlobalDockDoorClaimRule {
    fun check(item: GlobalDockDoorClaim): MutableList<String>
}

data class GlobalDockDoorClaimActiveGuardRule(var limit: Int) : GlobalDockDoorClaimRule {
    override fun check(item: GlobalDockDoorClaim): MutableList<String> {
        if (item.active && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDockDoorClaimCodeLengthRule(var maxLength: Int) : GlobalDockDoorClaimRule {
    override fun check(item: GlobalDockDoorClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDockDoorClaimMarginRangeRule(var min: Double, var max: Double) : GlobalDockDoorClaimRule {
    override fun check(item: GlobalDockDoorClaim): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDockDoorClaimCodePrefixRule(var prefix: String) : GlobalDockDoorClaimRule {
    override fun check(item: GlobalDockDoorClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDockDoorClaimExpiresAtRangeRule(var min: Int, var max: Int) : GlobalDockDoorClaimRule {
    override fun check(item: GlobalDockDoorClaim): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDockDoorClaimRequiredTitleRule(var message: String) : GlobalDockDoorClaimRule {
    override fun check(item: GlobalDockDoorClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global dock door claim. */
data class GlobalDockDoorClaimValidator(var rules: MutableList<GlobalDockDoorClaimRule>) {
    fun validate(item: GlobalDockDoorClaim): MutableList<String> {
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

    fun requireValid(item: GlobalDockDoorClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDockDoorClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDockDoorClaimValidator(): GlobalDockDoorClaimValidator {
    return GlobalDockDoorClaimValidator(
        mutableListOf<GlobalDockDoorClaimRule>(
            GlobalDockDoorClaimActiveGuardRule(3600),
            GlobalDockDoorClaimCodeLengthRule(80),
            GlobalDockDoorClaimMarginRangeRule(9.7, 3454.5),
            GlobalDockDoorClaimCodePrefixRule("ACM"),
            GlobalDockDoorClaimExpiresAtRangeRule(64, 255),
            GlobalDockDoorClaimRequiredTitleRule("global dock door claim needs a value"),
        ),
    )
}
