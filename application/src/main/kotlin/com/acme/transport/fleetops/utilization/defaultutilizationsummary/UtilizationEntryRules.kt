package com.acme.transport.fleetops.utilization.defaultutilizationsummary

/** A rule a utilization entry must pass. Check returns its problems, or none. */
interface UtilizationEntryRule {
    fun check(item: UtilizationEntry): MutableList<String>
}

data class UtilizationEntryRequiredCodeRule(var message: String) : UtilizationEntryRule {
    override fun check(item: UtilizationEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationEntryWindowDaysRangeRule(var min: Int, var max: Int) : UtilizationEntryRule {
    override fun check(item: UtilizationEntry): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class UtilizationEntryCodePrefixRule(var prefix: String) : UtilizationEntryRule {
    override fun check(item: UtilizationEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationEntryRequiredTitleRule(var message: String) : UtilizationEntryRule {
    override fun check(item: UtilizationEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationEntryExpiresAtRangeRule(var min: Int, var max: Int) : UtilizationEntryRule {
    override fun check(item: UtilizationEntry): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class UtilizationEntryActiveGuardRule(var limit: Double) : UtilizationEntryRule {
    override fun check(item: UtilizationEntry): MutableList<String> {
        if (item.active && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization entry. */
data class UtilizationEntryValidator(var rules: MutableList<UtilizationEntryRule>) {
    fun validate(item: UtilizationEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationEntryValidator(): UtilizationEntryValidator {
    return UtilizationEntryValidator(
        mutableListOf<UtilizationEntryRule>(
            UtilizationEntryRequiredCodeRule("utilization entry needs an entry"),
            UtilizationEntryWindowDaysRangeRule(64, 3600),
            UtilizationEntryCodePrefixRule("Q"),
            UtilizationEntryRequiredTitleRule("utilization entry needs an entry"),
            UtilizationEntryExpiresAtRangeRule(32, 64),
            UtilizationEntryActiveGuardRule(9.7),
        ),
    )
}
