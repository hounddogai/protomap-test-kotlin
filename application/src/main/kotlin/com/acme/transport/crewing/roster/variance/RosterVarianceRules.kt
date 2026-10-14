package com.acme.transport.crewing.roster.variance

/** A rule a roster variance must pass. Check returns its problems, or none. */
interface RosterVarianceRule {
    fun check(item: RosterVariance): MutableList<String>
}

data class RosterVarianceLabelLengthRule(var maxLength: Int) : RosterVarianceRule {
    override fun check(item: RosterVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterVarianceThresholdRangeRule(var min: Double, var max: Double) : RosterVarianceRule {
    override fun check(item: RosterVariance): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class RosterVarianceGraceDaysRangeRule(var min: Int, var max: Int) : RosterVarianceRule {
    override fun check(item: RosterVariance): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterVarianceExternalRefLengthRule(var maxLength: Int) : RosterVarianceRule {
    override fun check(item: RosterVariance): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterVarianceReviewerLengthRule(var maxLength: Int) : RosterVarianceRule {
    override fun check(item: RosterVariance): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster variance. */
data class RosterVarianceValidator(var rules: MutableList<RosterVarianceRule>) {
    fun validate(item: RosterVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterVarianceValidator(): RosterVarianceValidator {
    return RosterVarianceValidator(
        mutableListOf<RosterVarianceRule>(
            RosterVarianceLabelLengthRule(120),
            RosterVarianceThresholdRangeRule(4.3, 361.8),
            RosterVarianceGraceDaysRangeRule(32, 64),
            RosterVarianceExternalRefLengthRule(1000),
            RosterVarianceReviewerLengthRule(80),
        ),
    )
}
