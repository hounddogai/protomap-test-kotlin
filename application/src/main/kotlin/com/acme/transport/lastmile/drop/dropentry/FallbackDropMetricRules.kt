package com.acme.transport.lastmile.drop.dropentry

/** A rule a fallback drop metric must pass. Check returns its problems, or none. */
interface FallbackDropMetricRule {
    fun check(item: FallbackDropMetric): MutableList<String>
}

data class FallbackDropMetricAttemptsRangeRule(var min: Int, var max: Int) : FallbackDropMetricRule {
    override fun check(item: FallbackDropMetric): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDropMetricLabelLengthRule(var maxLength: Int) : FallbackDropMetricRule {
    override fun check(item: FallbackDropMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDropMetricBatchSizeRangeRule(var min: Int, var max: Int) : FallbackDropMetricRule {
    override fun check(item: FallbackDropMetric): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback drop metric. */
data class FallbackDropMetricValidator(var rules: MutableList<FallbackDropMetricRule>) {
    fun validate(item: FallbackDropMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDropMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDropMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDropMetricValidator(): FallbackDropMetricValidator {
    return FallbackDropMetricValidator(
        mutableListOf<FallbackDropMetricRule>(
            FallbackDropMetricAttemptsRangeRule(16, 32),
            FallbackDropMetricLabelLengthRule(120),
            FallbackDropMetricBatchSizeRangeRule(120, 255),
        ),
    )
}
