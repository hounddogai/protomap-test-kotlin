package com.acme.transport.lastmile.attempt.window

/** A rule a default attempt metric must pass. Check returns its problems, or none. */
interface DefaultAttemptMetricRule {
    fun check(item: DefaultAttemptMetric): MutableList<String>
}

data class DefaultAttemptMetricThresholdRangeRule(var min: Double, var max: Double) : DefaultAttemptMetricRule {
    override fun check(item: DefaultAttemptMetric): MutableList<String> {
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

data class DefaultAttemptMetricAttemptsRangeRule(var min: Int, var max: Int) : DefaultAttemptMetricRule {
    override fun check(item: DefaultAttemptMetric): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptMetricDurationMinutesRangeRule(var min: Int, var max: Int) : DefaultAttemptMetricRule {
    override fun check(item: DefaultAttemptMetric): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class DefaultAttemptMetricCodeLengthRule(var maxLength: Int) : DefaultAttemptMetricRule {
    override fun check(item: DefaultAttemptMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptMetricCodePrefixRule(var prefix: String) : DefaultAttemptMetricRule {
    override fun check(item: DefaultAttemptMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptMetricRequiredNameRule(var message: String) : DefaultAttemptMetricRule {
    override fun check(item: DefaultAttemptMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default attempt metric. */
data class DefaultAttemptMetricValidator(var rules: MutableList<DefaultAttemptMetricRule>) {
    fun validate(item: DefaultAttemptMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAttemptMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAttemptMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAttemptMetricValidator(): DefaultAttemptMetricValidator {
    return DefaultAttemptMetricValidator(
        mutableListOf<DefaultAttemptMetricRule>(
            DefaultAttemptMetricThresholdRangeRule(1.5, 2115.6),
            DefaultAttemptMetricAttemptsRangeRule(255, 3600),
            DefaultAttemptMetricDurationMinutesRangeRule(64, 3600),
            DefaultAttemptMetricCodeLengthRule(32),
            DefaultAttemptMetricCodePrefixRule("Q"),
            DefaultAttemptMetricRequiredNameRule("default attempt metric needs this field"),
        ),
    )
}
