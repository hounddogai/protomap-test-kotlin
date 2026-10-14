package com.acme.transport.fleetops.leasereturn.entry

/** A rule a regional lease return entry must pass. Check returns its problems, or none. */
interface RegionalLeaseReturnEntryRule {
    fun check(item: RegionalLeaseReturnEntry): MutableList<String>
}

data class RegionalLeaseReturnEntryCodePrefixRule(var prefix: String) : RegionalLeaseReturnEntryRule {
    override fun check(item: RegionalLeaseReturnEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnEntryRequiredNameRule(var message: String) : RegionalLeaseReturnEntryRule {
    override fun check(item: RegionalLeaseReturnEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnEntryRequiredCodeRule(var message: String) : RegionalLeaseReturnEntryRule {
    override fun check(item: RegionalLeaseReturnEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnEntryCodeLengthRule(var maxLength: Int) : RegionalLeaseReturnEntryRule {
    override fun check(item: RegionalLeaseReturnEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnEntryGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : RegionalLeaseReturnEntryRule {
    override fun check(item: RegionalLeaseReturnEntry): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional lease return entry. */
data class RegionalLeaseReturnEntryValidator(var rules: MutableList<RegionalLeaseReturnEntryRule>) {
    fun validate(item: RegionalLeaseReturnEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalLeaseReturnEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLeaseReturnEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLeaseReturnEntryValidator(): RegionalLeaseReturnEntryValidator {
    return RegionalLeaseReturnEntryValidator(
        mutableListOf<RegionalLeaseReturnEntryRule>(
            RegionalLeaseReturnEntryCodePrefixRule("ACM"),
            RegionalLeaseReturnEntryRequiredNameRule("regional lease return entry needs this field"),
            RegionalLeaseReturnEntryRequiredCodeRule("regional lease return entry needs this field"),
            RegionalLeaseReturnEntryCodeLengthRule(255),
            RegionalLeaseReturnEntryGraceDaysRangeRule(80, 1000),
        ),
    )
}
