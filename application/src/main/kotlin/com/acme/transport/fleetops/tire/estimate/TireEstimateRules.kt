package com.acme.transport.fleetops.tire.estimate

/** A rule a tire estimate must pass. Check returns its problems, or none. */
interface TireEstimateRule {
    fun check(item: TireEstimate): MutableList<String>
}

data class TireEstimateWeightRangeRule(var min: Double, var max: Double) : TireEstimateRule {
    override fun check(item: TireEstimate): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class TireEstimateRequiredCodeRule(var message: String) : TireEstimateRule {
    override fun check(item: TireEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireEstimateLabelLengthRule(var maxLength: Int) : TireEstimateRule {
    override fun check(item: TireEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire estimate. */
data class TireEstimateValidator(var rules: MutableList<TireEstimateRule>) {
    fun validate(item: TireEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultTireEstimateValidator(): TireEstimateValidator {
    return TireEstimateValidator(
        mutableListOf<TireEstimateRule>(
            TireEstimateWeightRangeRule(6.2, 2372.4),
            TireEstimateRequiredCodeRule("tire estimate needs an entry"),
            TireEstimateLabelLengthRule(16),
        ),
    )
}
