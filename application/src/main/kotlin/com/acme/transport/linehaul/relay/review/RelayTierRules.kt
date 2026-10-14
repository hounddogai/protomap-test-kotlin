package com.acme.transport.linehaul.relay.review

/** A rule a relay tier must pass. Check returns its problems, or none. */
interface RelayTierRule {
    fun check(item: RelayTier): MutableList<String>
}

data class RelayTierRequiredNameRule(var message: String) : RelayTierRule {
    override fun check(item: RelayTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayTierExternalRefLengthRule(var maxLength: Int) : RelayTierRule {
    override fun check(item: RelayTier): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayTierCodeLengthRule(var maxLength: Int) : RelayTierRule {
    override fun check(item: RelayTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayTierCodePrefixRule(var prefix: String) : RelayTierRule {
    override fun check(item: RelayTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RelayTierActiveGuardRule(var limit: Int) : RelayTierRule {
    override fun check(item: RelayTier): MutableList<String> {
        if (item.active && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay tier. */
data class RelayTierValidator(var rules: MutableList<RelayTierRule>) {
    fun validate(item: RelayTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayTierValidator(): RelayTierValidator {
    return RelayTierValidator(
        mutableListOf<RelayTierRule>(
            RelayTierRequiredNameRule("relay tier needs a value"),
            RelayTierExternalRefLengthRule(86400),
            RelayTierCodeLengthRule(86400),
            RelayTierCodePrefixRule("ACM"),
            RelayTierActiveGuardRule(16),
        ),
    )
}
