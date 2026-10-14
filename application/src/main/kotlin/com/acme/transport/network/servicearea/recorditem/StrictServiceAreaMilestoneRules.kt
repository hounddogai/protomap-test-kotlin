package com.acme.transport.network.servicearea.recorditem

/** A rule a strict service area milestone must pass. Check returns its problems, or none. */
interface StrictServiceAreaMilestoneRule {
    fun check(item: StrictServiceAreaMilestone): MutableList<String>
}

data class StrictServiceAreaMilestoneCodeLengthRule(var maxLength: Int) : StrictServiceAreaMilestoneRule {
    override fun check(item: StrictServiceAreaMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaMilestoneTimezoneLengthRule(var maxLength: Int) : StrictServiceAreaMilestoneRule {
    override fun check(item: StrictServiceAreaMilestone): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaMilestoneDiscountRangeRule(
    var min: Double,
    var max: Double,
) : StrictServiceAreaMilestoneRule {
    override fun check(item: StrictServiceAreaMilestone): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class StrictServiceAreaMilestoneExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : StrictServiceAreaMilestoneRule {
    override fun check(item: StrictServiceAreaMilestone): MutableList<String> {
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

/** Runs every rule of a strict service area milestone. */
data class StrictServiceAreaMilestoneValidator(var rules: MutableList<StrictServiceAreaMilestoneRule>) {
    fun validate(item: StrictServiceAreaMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictServiceAreaMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictServiceAreaMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictServiceAreaMilestoneValidator(): StrictServiceAreaMilestoneValidator {
    return StrictServiceAreaMilestoneValidator(
        mutableListOf<StrictServiceAreaMilestoneRule>(
            StrictServiceAreaMilestoneCodeLengthRule(16),
            StrictServiceAreaMilestoneTimezoneLengthRule(1000),
            StrictServiceAreaMilestoneDiscountRangeRule(3.5, 97.4),
            StrictServiceAreaMilestoneExpiresAtRangeRule(80, 3600),
        ),
    )
}
