package com.acme.transport.crewing.roster.entry

/** A rule a global roster entry must pass. Check returns its problems, or none. */
interface GlobalRosterEntryRule {
    fun check(item: GlobalRosterEntry): MutableList<String>
}

data class GlobalRosterEntryCodePrefixRule(var prefix: String) : GlobalRosterEntryRule {
    override fun check(item: GlobalRosterEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterEntryTrailerIdLengthRule(var maxLength: Int) : GlobalRosterEntryRule {
    override fun check(item: GlobalRosterEntry): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterEntryRequiredCurrencyRule(var message: String) : GlobalRosterEntryRule {
    override fun check(item: GlobalRosterEntry): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterEntryRequiredTrailerIdRule(var message: String) : GlobalRosterEntryRule {
    override fun check(item: GlobalRosterEntry): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterEntryWeightRangeRule(var min: Double, var max: Double) : GlobalRosterEntryRule {
    override fun check(item: GlobalRosterEntry): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global roster entry. */
data class GlobalRosterEntryValidator(var rules: MutableList<GlobalRosterEntryRule>) {
    fun validate(item: GlobalRosterEntry): MutableList<String> {
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

    fun requireValid(item: GlobalRosterEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRosterEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRosterEntryValidator(): GlobalRosterEntryValidator {
    return GlobalRosterEntryValidator(
        mutableListOf<GlobalRosterEntryRule>(
            GlobalRosterEntryCodePrefixRule("ACM"),
            GlobalRosterEntryTrailerIdLengthRule(80),
            GlobalRosterEntryRequiredCurrencyRule("global roster entry needs an entry"),
            GlobalRosterEntryRequiredTrailerIdRule("global roster entry needs a value"),
            GlobalRosterEntryWeightRangeRule(2.7, 1486.5),
        ),
    )
}
