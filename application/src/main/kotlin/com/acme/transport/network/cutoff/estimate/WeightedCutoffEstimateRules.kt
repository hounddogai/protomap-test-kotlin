package com.acme.transport.network.cutoff.estimate

/** A rule a weighted cutoff estimate must pass. Check returns its problems, or none. */
interface WeightedCutoffEstimateRule {
    fun check(item: WeightedCutoffEstimate): MutableList<String>
}

data class WeightedCutoffEstimateCostRangeRule(var min: Double, var max: Double) : WeightedCutoffEstimateRule {
    override fun check(item: WeightedCutoffEstimate): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCutoffEstimateRequiredShiftCodeRule(var message: String) : WeightedCutoffEstimateRule {
    override fun check(item: WeightedCutoffEstimate): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCutoffEstimateRequiredLabelRule(var message: String) : WeightedCutoffEstimateRule {
    override fun check(item: WeightedCutoffEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCutoffEstimateRequiredCodeRule(var message: String) : WeightedCutoffEstimateRule {
    override fun check(item: WeightedCutoffEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted cutoff estimate. */
data class WeightedCutoffEstimateValidator(var rules: MutableList<WeightedCutoffEstimateRule>) {
    fun validate(item: WeightedCutoffEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedCutoffEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCutoffEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCutoffEstimateValidator(): WeightedCutoffEstimateValidator {
    return WeightedCutoffEstimateValidator(
        mutableListOf<WeightedCutoffEstimateRule>(
            WeightedCutoffEstimateCostRangeRule(9.4, 4166.0),
            WeightedCutoffEstimateRequiredShiftCodeRule("weighted cutoff estimate needs a value"),
            WeightedCutoffEstimateRequiredLabelRule("weighted cutoff estimate needs an entry"),
            WeightedCutoffEstimateRequiredCodeRule("weighted cutoff estimate needs a value"),
        ),
    )
}
