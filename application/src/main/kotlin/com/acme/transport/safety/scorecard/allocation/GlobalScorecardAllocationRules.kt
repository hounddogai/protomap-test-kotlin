package com.acme.transport.safety.scorecard.allocation

/** A rule a global scorecard allocation must pass. Check returns its problems, or none. */
interface GlobalScorecardAllocationRule {
    fun check(item: GlobalScorecardAllocation): MutableList<String>
}

data class GlobalScorecardAllocationCodeLengthRule(var maxLength: Int) : GlobalScorecardAllocationRule {
    override fun check(item: GlobalScorecardAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalScorecardAllocationEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : GlobalScorecardAllocationRule {
    override fun check(item: GlobalScorecardAllocation): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalScorecardAllocationRequiredPhoneRule(var message: String) : GlobalScorecardAllocationRule {
    override fun check(item: GlobalScorecardAllocation): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalScorecardAllocationRequiredCodeRule(var message: String) : GlobalScorecardAllocationRule {
    override fun check(item: GlobalScorecardAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalScorecardAllocationCodePrefixRule(var prefix: String) : GlobalScorecardAllocationRule {
    override fun check(item: GlobalScorecardAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalScorecardAllocationWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : GlobalScorecardAllocationRule {
    override fun check(item: GlobalScorecardAllocation): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global scorecard allocation. */
data class GlobalScorecardAllocationValidator(var rules: MutableList<GlobalScorecardAllocationRule>) {
    fun validate(item: GlobalScorecardAllocation): MutableList<String> {
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

    fun requireValid(item: GlobalScorecardAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalScorecardAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalScorecardAllocationValidator(): GlobalScorecardAllocationValidator {
    return GlobalScorecardAllocationValidator(
        mutableListOf<GlobalScorecardAllocationRule>(
            GlobalScorecardAllocationCodeLengthRule(16),
            GlobalScorecardAllocationEndsAtRangeRule(120, 3600),
            GlobalScorecardAllocationRequiredPhoneRule("global scorecard allocation needs an entry"),
            GlobalScorecardAllocationRequiredCodeRule("global scorecard allocation needs this field"),
            GlobalScorecardAllocationCodePrefixRule("Q"),
            GlobalScorecardAllocationWindowDaysRangeRule(16, 80),
        ),
    )
}
