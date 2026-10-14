package com.acme.transport.safety.violation.violationslot

/** A rule a strict violation milestone must pass. Check returns its problems, or none. */
interface StrictViolationMilestoneRule {
    fun check(item: StrictViolationMilestone): MutableList<String>
}

data class StrictViolationMilestoneUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : StrictViolationMilestoneRule {
    override fun check(item: StrictViolationMilestone): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationMilestoneRequiredCurrencyRule(var message: String) : StrictViolationMilestoneRule {
    override fun check(item: StrictViolationMilestone): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationMilestoneCurrencyLengthRule(var maxLength: Int) : StrictViolationMilestoneRule {
    override fun check(item: StrictViolationMilestone): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict violation milestone. */
data class StrictViolationMilestoneValidator(var rules: MutableList<StrictViolationMilestoneRule>) {
    fun validate(item: StrictViolationMilestone): MutableList<String> {
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

    fun requireValid(item: StrictViolationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictViolationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictViolationMilestoneValidator(): StrictViolationMilestoneValidator {
    return StrictViolationMilestoneValidator(
        mutableListOf<StrictViolationMilestoneRule>(
            StrictViolationMilestoneUpdatedAtRangeRule(80, 86400),
            StrictViolationMilestoneRequiredCurrencyRule("strict violation milestone needs a value"),
            StrictViolationMilestoneCurrencyLengthRule(3600),
        ),
    )
}
