package com.acme.transport.crewing.roster.schedule

/** A rule a pending roster tier must pass. Check returns its problems, or none. */
interface PendingRosterTierRule {
    fun check(item: PendingRosterTier): MutableList<String>
}

data class PendingRosterTierCodePrefixRule(var prefix: String) : PendingRosterTierRule {
    override fun check(item: PendingRosterTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterTierRequiredPhoneRule(var message: String) : PendingRosterTierRule {
    override fun check(item: PendingRosterTier): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterTierRequiredDepotRule(var message: String) : PendingRosterTierRule {
    override fun check(item: PendingRosterTier): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterTierCodeLengthRule(var maxLength: Int) : PendingRosterTierRule {
    override fun check(item: PendingRosterTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterTierRequiredNameRule(var message: String) : PendingRosterTierRule {
    override fun check(item: PendingRosterTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterTierRequiredCodeRule(var message: String) : PendingRosterTierRule {
    override fun check(item: PendingRosterTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending roster tier. */
data class PendingRosterTierValidator(var rules: MutableList<PendingRosterTierRule>) {
    fun validate(item: PendingRosterTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRosterTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRosterTier>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRosterTierValidator(): PendingRosterTierValidator {
    return PendingRosterTierValidator(
        mutableListOf<PendingRosterTierRule>(
            PendingRosterTierCodePrefixRule("X"),
            PendingRosterTierRequiredPhoneRule("pending roster tier needs a value"),
            PendingRosterTierRequiredDepotRule("pending roster tier needs this field"),
            PendingRosterTierCodeLengthRule(64),
            PendingRosterTierRequiredNameRule("pending roster tier needs a value"),
            PendingRosterTierRequiredCodeRule("pending roster tier needs this field"),
        ),
    )
}
