package com.acme.transport.safety.scorecard.scorecardslot

/** A rule a default scorecard allocation must pass. Check returns its problems, or none. */
interface DefaultScorecardAllocationRule {
    fun check(item: DefaultScorecardAllocation): MutableList<String>
}

data class DefaultScorecardAllocationVisibleGuardRule(var limit: Int) : DefaultScorecardAllocationRule {
    override fun check(item: DefaultScorecardAllocation): MutableList<String> {
        if (item.visible && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardAllocationRequiredTitleRule(var message: String) : DefaultScorecardAllocationRule {
    override fun check(item: DefaultScorecardAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardAllocationCodePrefixRule(var prefix: String) : DefaultScorecardAllocationRule {
    override fun check(item: DefaultScorecardAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardAllocationRequiredPhoneRule(var message: String) : DefaultScorecardAllocationRule {
    override fun check(item: DefaultScorecardAllocation): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardAllocationDepotLengthRule(var maxLength: Int) : DefaultScorecardAllocationRule {
    override fun check(item: DefaultScorecardAllocation): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardAllocationRequiredCodeRule(var message: String) : DefaultScorecardAllocationRule {
    override fun check(item: DefaultScorecardAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default scorecard allocation. */
data class DefaultScorecardAllocationValidator(var rules: MutableList<DefaultScorecardAllocationRule>) {
    fun validate(item: DefaultScorecardAllocation): MutableList<String> {
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

    fun requireValid(item: DefaultScorecardAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultScorecardAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultScorecardAllocationValidator(): DefaultScorecardAllocationValidator {
    return DefaultScorecardAllocationValidator(
        mutableListOf<DefaultScorecardAllocationRule>(
            DefaultScorecardAllocationVisibleGuardRule(120),
            DefaultScorecardAllocationRequiredTitleRule("default scorecard allocation needs an entry"),
            DefaultScorecardAllocationCodePrefixRule("ACM"),
            DefaultScorecardAllocationRequiredPhoneRule("default scorecard allocation needs an entry"),
            DefaultScorecardAllocationDepotLengthRule(1000),
            DefaultScorecardAllocationRequiredCodeRule("default scorecard allocation needs an entry"),
        ),
    )
}
