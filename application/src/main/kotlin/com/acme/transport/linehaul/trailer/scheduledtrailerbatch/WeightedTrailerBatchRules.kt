package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

/** A rule a weighted trailer batch must pass. Check returns its problems, or none. */
interface WeightedTrailerBatchRule {
    fun check(item: WeightedTrailerBatch): MutableList<String>
}

data class WeightedTrailerBatchUpdatedAtRangeRule(var min: Int, var max: Int) : WeightedTrailerBatchRule {
    override fun check(item: WeightedTrailerBatch): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class WeightedTrailerBatchCodeLengthRule(var maxLength: Int) : WeightedTrailerBatchRule {
    override fun check(item: WeightedTrailerBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerBatchThresholdRangeRule(var min: Double, var max: Double) : WeightedTrailerBatchRule {
    override fun check(item: WeightedTrailerBatch): MutableList<String> {
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

/** Runs every rule of a weighted trailer batch. */
data class WeightedTrailerBatchValidator(var rules: MutableList<WeightedTrailerBatchRule>) {
    fun validate(item: WeightedTrailerBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTrailerBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTrailerBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTrailerBatchValidator(): WeightedTrailerBatchValidator {
    return WeightedTrailerBatchValidator(
        mutableListOf<WeightedTrailerBatchRule>(
            WeightedTrailerBatchUpdatedAtRangeRule(32, 255),
            WeightedTrailerBatchCodeLengthRule(86400),
            WeightedTrailerBatchThresholdRangeRule(9.5, 3366.7),
        ),
    )
}
