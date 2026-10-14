package com.acme.transport.safety.violation.violationpolicy

/** A rule a seasonal violation claim must pass. Check returns its problems, or none. */
interface SeasonalViolationClaimRule {
    fun check(item: SeasonalViolationClaim): MutableList<String>
}

data class SeasonalViolationClaimContactEmailLengthRule(var maxLength: Int) : SeasonalViolationClaimRule {
    override fun check(item: SeasonalViolationClaim): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationClaimEndsAtRangeRule(var min: Int, var max: Int) : SeasonalViolationClaimRule {
    override fun check(item: SeasonalViolationClaim): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationClaimRequiredCodeRule(var message: String) : SeasonalViolationClaimRule {
    override fun check(item: SeasonalViolationClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationClaimSegmentLengthRule(var maxLength: Int) : SeasonalViolationClaimRule {
    override fun check(item: SeasonalViolationClaim): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationClaimMaximumRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalViolationClaimRule {
    override fun check(item: SeasonalViolationClaim): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalViolationClaimGraceDaysRangeRule(var min: Int, var max: Int) : SeasonalViolationClaimRule {
    override fun check(item: SeasonalViolationClaim): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal violation claim. */
data class SeasonalViolationClaimValidator(var rules: MutableList<SeasonalViolationClaimRule>) {
    fun validate(item: SeasonalViolationClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalViolationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalViolationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalViolationClaimValidator(): SeasonalViolationClaimValidator {
    return SeasonalViolationClaimValidator(
        mutableListOf<SeasonalViolationClaimRule>(
            SeasonalViolationClaimContactEmailLengthRule(64),
            SeasonalViolationClaimEndsAtRangeRule(64, 1000),
            SeasonalViolationClaimRequiredCodeRule("seasonal violation claim needs a value"),
            SeasonalViolationClaimSegmentLengthRule(1000),
            SeasonalViolationClaimMaximumRangeRule(0.1, 2817.3),
            SeasonalViolationClaimGraceDaysRangeRule(80, 255),
        ),
    )
}
