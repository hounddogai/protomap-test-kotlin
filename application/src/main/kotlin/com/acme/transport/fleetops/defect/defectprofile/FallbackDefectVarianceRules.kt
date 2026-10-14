package com.acme.transport.fleetops.defect.defectprofile

/** A rule a fallback defect variance must pass. Check returns its problems, or none. */
interface FallbackDefectVarianceRule {
    fun check(item: FallbackDefectVariance): MutableList<String>
}

data class FallbackDefectVarianceAttemptsRangeRule(var min: Int, var max: Int) : FallbackDefectVarianceRule {
    override fun check(item: FallbackDefectVariance): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class FallbackDefectVarianceRequiredLabelRule(var message: String) : FallbackDefectVarianceRule {
    override fun check(item: FallbackDefectVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectVarianceGraceDaysRangeRule(var min: Int, var max: Int) : FallbackDefectVarianceRule {
    override fun check(item: FallbackDefectVariance): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectVarianceRequiredCodeRule(var message: String) : FallbackDefectVarianceRule {
    override fun check(item: FallbackDefectVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectVarianceCapacityRangeRule(var min: Int, var max: Int) : FallbackDefectVarianceRule {
    override fun check(item: FallbackDefectVariance): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class FallbackDefectVarianceReviewerLengthRule(var maxLength: Int) : FallbackDefectVarianceRule {
    override fun check(item: FallbackDefectVariance): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback defect variance. */
data class FallbackDefectVarianceValidator(var rules: MutableList<FallbackDefectVarianceRule>) {
    fun validate(item: FallbackDefectVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDefectVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDefectVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDefectVarianceValidator(): FallbackDefectVarianceValidator {
    return FallbackDefectVarianceValidator(
        mutableListOf<FallbackDefectVarianceRule>(
            FallbackDefectVarianceAttemptsRangeRule(80, 80),
            FallbackDefectVarianceRequiredLabelRule("fallback defect variance needs an entry"),
            FallbackDefectVarianceGraceDaysRangeRule(120, 86400),
            FallbackDefectVarianceRequiredCodeRule("fallback defect variance needs this field"),
            FallbackDefectVarianceCapacityRangeRule(16, 1000),
            FallbackDefectVarianceReviewerLengthRule(86400),
        ),
    )
}
