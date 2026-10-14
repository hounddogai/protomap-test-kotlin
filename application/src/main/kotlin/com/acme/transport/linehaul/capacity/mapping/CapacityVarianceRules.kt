package com.acme.transport.linehaul.capacity.mapping

/** A rule a capacity variance must pass. Check returns its problems, or none. */
interface CapacityVarianceRule {
    fun check(item: CapacityVariance): MutableList<String>
}

data class CapacityVarianceRequiredLabelRule(var message: String) : CapacityVarianceRule {
    override fun check(item: CapacityVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityVarianceQuantityRangeRule(var min: Int, var max: Int) : CapacityVarianceRule {
    override fun check(item: CapacityVariance): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityVarianceCostRangeRule(var min: Double, var max: Double) : CapacityVarianceRule {
    override fun check(item: CapacityVariance): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capacity variance. */
data class CapacityVarianceValidator(var rules: MutableList<CapacityVarianceRule>) {
    fun validate(item: CapacityVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityVarianceValidator(): CapacityVarianceValidator {
    return CapacityVarianceValidator(
        mutableListOf<CapacityVarianceRule>(
            CapacityVarianceRequiredLabelRule("capacity variance needs a value"),
            CapacityVarianceQuantityRangeRule(86400, 86400),
            CapacityVarianceCostRangeRule(2.9, 335.2),
        ),
    )
}
