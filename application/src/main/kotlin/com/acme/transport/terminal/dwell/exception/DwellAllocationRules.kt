package com.acme.transport.terminal.dwell.exception

/** A rule a dwell allocation must pass. Check returns its problems, or none. */
interface DwellAllocationRule {
    fun check(item: DwellAllocation): MutableList<String>
}

data class DwellAllocationRequiredCurrencyRule(var message: String) : DwellAllocationRule {
    override fun check(item: DwellAllocation): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellAllocationExpiresAtRangeRule(var min: Int, var max: Int) : DwellAllocationRule {
    override fun check(item: DwellAllocation): MutableList<String> {
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

data class DwellAllocationRetriesRangeRule(var min: Int, var max: Int) : DwellAllocationRule {
    override fun check(item: DwellAllocation): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DwellAllocationCodePrefixRule(var prefix: String) : DwellAllocationRule {
    override fun check(item: DwellAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell allocation. */
data class DwellAllocationValidator(var rules: MutableList<DwellAllocationRule>) {
    fun validate(item: DwellAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellAllocationValidator(): DwellAllocationValidator {
    return DwellAllocationValidator(
        mutableListOf<DwellAllocationRule>(
            DwellAllocationRequiredCurrencyRule("dwell allocation needs an entry"),
            DwellAllocationExpiresAtRangeRule(1000, 3600),
            DwellAllocationRetriesRangeRule(64, 1000),
            DwellAllocationCodePrefixRule("ACM"),
        ),
    )
}
