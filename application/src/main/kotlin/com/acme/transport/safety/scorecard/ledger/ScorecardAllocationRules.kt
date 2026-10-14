package com.acme.transport.safety.scorecard.ledger

/** A rule a scorecard allocation must pass. Check returns its problems, or none. */
interface ScorecardAllocationRule {
    fun check(item: ScorecardAllocation): MutableList<String>
}

data class ScorecardAllocationCapacityRangeRule(var min: Int, var max: Int) : ScorecardAllocationRule {
    override fun check(item: ScorecardAllocation): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAllocationCodePrefixRule(var prefix: String) : ScorecardAllocationRule {
    override fun check(item: ScorecardAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAllocationRequiredCountryRule(var message: String) : ScorecardAllocationRule {
    override fun check(item: ScorecardAllocation): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAllocationRequiredDriverIdRule(var message: String) : ScorecardAllocationRule {
    override fun check(item: ScorecardAllocation): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAllocationCountryLengthRule(var maxLength: Int) : ScorecardAllocationRule {
    override fun check(item: ScorecardAllocation): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardAllocationRequiredCodeRule(var message: String) : ScorecardAllocationRule {
    override fun check(item: ScorecardAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard allocation. */
data class ScorecardAllocationValidator(var rules: MutableList<ScorecardAllocationRule>) {
    fun validate(item: ScorecardAllocation): MutableList<String> {
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

    fun requireValid(item: ScorecardAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardAllocationValidator(): ScorecardAllocationValidator {
    return ScorecardAllocationValidator(
        mutableListOf<ScorecardAllocationRule>(
            ScorecardAllocationCapacityRangeRule(80, 86400),
            ScorecardAllocationCodePrefixRule("Q"),
            ScorecardAllocationRequiredCountryRule("scorecard allocation needs a value"),
            ScorecardAllocationRequiredDriverIdRule("scorecard allocation needs a value"),
            ScorecardAllocationCountryLengthRule(1000),
            ScorecardAllocationRequiredCodeRule("scorecard allocation needs an entry"),
        ),
    )
}
