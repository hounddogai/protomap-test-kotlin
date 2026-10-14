package com.acme.transport.linehaul.trailer.rule

/** A rule a trailer variance must pass. Check returns its problems, or none. */
interface TrailerVarianceRule {
    fun check(item: TrailerVariance): MutableList<String>
}

data class TrailerVarianceThresholdRangeRule(var min: Double, var max: Double) : TrailerVarianceRule {
    override fun check(item: TrailerVariance): MutableList<String> {
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

data class TrailerVarianceRequiredLaneRule(var message: String) : TrailerVarianceRule {
    override fun check(item: TrailerVariance): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerVarianceCeilingRangeRule(var min: Double, var max: Double) : TrailerVarianceRule {
    override fun check(item: TrailerVariance): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer variance. */
data class TrailerVarianceValidator(var rules: MutableList<TrailerVarianceRule>) {
    fun validate(item: TrailerVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerVarianceValidator(): TrailerVarianceValidator {
    return TrailerVarianceValidator(
        mutableListOf<TrailerVarianceRule>(
            TrailerVarianceThresholdRangeRule(7.1, 2606.1),
            TrailerVarianceRequiredLaneRule("trailer variance needs a value"),
            TrailerVarianceCeilingRangeRule(7.7, 380.4),
        ),
    )
}
