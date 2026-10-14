package com.acme.transport.freightpay.ratecard.cardgroup

/** A rule a strict rate card assignment must pass. Check returns its problems, or none. */
interface StrictRateCardAssignmentRule {
    fun check(item: StrictRateCardAssignment): MutableList<String>
}

data class StrictRateCardAssignmentEndsAtRangeRule(var min: Int, var max: Int) : StrictRateCardAssignmentRule {
    override fun check(item: StrictRateCardAssignment): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardAssignmentRequiredCodeRule(var message: String) : StrictRateCardAssignmentRule {
    override fun check(item: StrictRateCardAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardAssignmentCodePrefixRule(var prefix: String) : StrictRateCardAssignmentRule {
    override fun check(item: StrictRateCardAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rate card assignment. */
data class StrictRateCardAssignmentValidator(var rules: MutableList<StrictRateCardAssignmentRule>) {
    fun validate(item: StrictRateCardAssignment): MutableList<String> {
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

    fun requireValid(item: StrictRateCardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRateCardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRateCardAssignmentValidator(): StrictRateCardAssignmentValidator {
    return StrictRateCardAssignmentValidator(
        mutableListOf<StrictRateCardAssignmentRule>(
            StrictRateCardAssignmentEndsAtRangeRule(64, 86400),
            StrictRateCardAssignmentRequiredCodeRule("strict rate card assignment needs an entry"),
            StrictRateCardAssignmentCodePrefixRule("REF"),
        ),
    )
}
