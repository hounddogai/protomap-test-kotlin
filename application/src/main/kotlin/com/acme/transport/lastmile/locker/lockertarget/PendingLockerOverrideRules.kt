package com.acme.transport.lastmile.locker.lockertarget

/** A rule a pending locker override must pass. Check returns its problems, or none. */
interface PendingLockerOverrideRule {
    fun check(item: PendingLockerOverride): MutableList<String>
}

data class PendingLockerOverrideRequiredTitleRule(var message: String) : PendingLockerOverrideRule {
    override fun check(item: PendingLockerOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerOverrideRequiredCurrencyRule(var message: String) : PendingLockerOverrideRule {
    override fun check(item: PendingLockerOverride): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerOverrideCodePrefixRule(var prefix: String) : PendingLockerOverrideRule {
    override fun check(item: PendingLockerOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending locker override. */
data class PendingLockerOverrideValidator(var rules: MutableList<PendingLockerOverrideRule>) {
    fun validate(item: PendingLockerOverride): MutableList<String> {
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

    fun requireValid(item: PendingLockerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLockerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLockerOverrideValidator(): PendingLockerOverrideValidator {
    return PendingLockerOverrideValidator(
        mutableListOf<PendingLockerOverrideRule>(
            PendingLockerOverrideRequiredTitleRule("pending locker override needs this field"),
            PendingLockerOverrideRequiredCurrencyRule("pending locker override needs a value"),
            PendingLockerOverrideCodePrefixRule("Q"),
        ),
    )
}
