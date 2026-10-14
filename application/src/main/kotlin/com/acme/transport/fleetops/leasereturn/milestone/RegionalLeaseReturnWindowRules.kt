package com.acme.transport.fleetops.leasereturn.milestone

/** A rule a regional lease return window must pass. Check returns its problems, or none. */
interface RegionalLeaseReturnWindowRule {
    fun check(item: RegionalLeaseReturnWindow): MutableList<String>
}

data class RegionalLeaseReturnWindowCodePrefixRule(var prefix: String) : RegionalLeaseReturnWindowRule {
    override fun check(item: RegionalLeaseReturnWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnWindowRequiredSummaryRule(var message: String) : RegionalLeaseReturnWindowRule {
    override fun check(item: RegionalLeaseReturnWindow): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnWindowNameLengthRule(var maxLength: Int) : RegionalLeaseReturnWindowRule {
    override fun check(item: RegionalLeaseReturnWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional lease return window. */
data class RegionalLeaseReturnWindowValidator(var rules: MutableList<RegionalLeaseReturnWindowRule>) {
    fun validate(item: RegionalLeaseReturnWindow): MutableList<String> {
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

    fun requireValid(item: RegionalLeaseReturnWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLeaseReturnWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLeaseReturnWindowValidator(): RegionalLeaseReturnWindowValidator {
    return RegionalLeaseReturnWindowValidator(
        mutableListOf<RegionalLeaseReturnWindowRule>(
            RegionalLeaseReturnWindowCodePrefixRule("REF"),
            RegionalLeaseReturnWindowRequiredSummaryRule("regional lease return window needs an entry"),
            RegionalLeaseReturnWindowNameLengthRule(86400),
        ),
    )
}
