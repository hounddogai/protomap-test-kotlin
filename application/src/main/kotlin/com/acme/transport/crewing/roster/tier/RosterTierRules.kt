package com.acme.transport.crewing.roster.tier

/** A rule a roster tier must pass. Check returns its problems, or none. */
interface RosterTierRule {
    fun check(item: RosterTier): MutableList<String>
}

data class RosterTierRequiredTitleRule(var message: String) : RosterTierRule {
    override fun check(item: RosterTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterTierDriverIdLengthRule(var maxLength: Int) : RosterTierRule {
    override fun check(item: RosterTier): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterTierCodePrefixRule(var prefix: String) : RosterTierRule {
    override fun check(item: RosterTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster tier. */
data class RosterTierValidator(var rules: MutableList<RosterTierRule>) {
    fun validate(item: RosterTier): MutableList<String> {
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

    fun requireValid(item: RosterTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterTierValidator(): RosterTierValidator {
    return RosterTierValidator(
        mutableListOf<RosterTierRule>(
            RosterTierRequiredTitleRule("roster tier needs a value"),
            RosterTierDriverIdLengthRule(86400),
            RosterTierCodePrefixRule("OPS"),
        ),
    )
}
