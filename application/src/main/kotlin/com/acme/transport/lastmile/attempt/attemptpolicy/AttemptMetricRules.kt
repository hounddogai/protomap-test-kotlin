package com.acme.transport.lastmile.attempt.attemptpolicy

/** A rule a attempt metric must pass. Check returns its problems, or none. */
interface AttemptMetricRule {
    fun check(item: AttemptMetric): MutableList<String>
}

data class AttemptMetricFuelLitersRangeRule(var min: Double, var max: Double) : AttemptMetricRule {
    override fun check(item: AttemptMetric): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class AttemptMetricRequiredCodeRule(var message: String) : AttemptMetricRule {
    override fun check(item: AttemptMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptMetricCapacityRangeRule(var min: Int, var max: Int) : AttemptMetricRule {
    override fun check(item: AttemptMetric): MutableList<String> {
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

data class AttemptMetricCodeLengthRule(var maxLength: Int) : AttemptMetricRule {
    override fun check(item: AttemptMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptMetricDescriptionLengthRule(var maxLength: Int) : AttemptMetricRule {
    override fun check(item: AttemptMetric): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptMetricCodePrefixRule(var prefix: String) : AttemptMetricRule {
    override fun check(item: AttemptMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt metric. */
data class AttemptMetricValidator(var rules: MutableList<AttemptMetricRule>) {
    fun validate(item: AttemptMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptMetricValidator(): AttemptMetricValidator {
    return AttemptMetricValidator(
        mutableListOf<AttemptMetricRule>(
            AttemptMetricFuelLitersRangeRule(5.5, 1239.2),
            AttemptMetricRequiredCodeRule("attempt metric needs a value"),
            AttemptMetricCapacityRangeRule(16, 16),
            AttemptMetricCodeLengthRule(32),
            AttemptMetricDescriptionLengthRule(32),
            AttemptMetricCodePrefixRule("OPS"),
        ),
    )
}
