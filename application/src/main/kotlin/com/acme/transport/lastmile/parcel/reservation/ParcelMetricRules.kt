package com.acme.transport.lastmile.parcel.reservation

/** A rule a parcel metric must pass. Check returns its problems, or none. */
interface ParcelMetricRule {
    fun check(item: ParcelMetric): MutableList<String>
}

data class ParcelMetricCostRangeRule(var min: Double, var max: Double) : ParcelMetricRule {
    override fun check(item: ParcelMetric): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ParcelMetricRequiredCodeRule(var message: String) : ParcelMetricRule {
    override fun check(item: ParcelMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelMetricBudgetRangeRule(var min: Double, var max: Double) : ParcelMetricRule {
    override fun check(item: ParcelMetric): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class ParcelMetricRequiredSummaryRule(var message: String) : ParcelMetricRule {
    override fun check(item: ParcelMetric): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel metric. */
data class ParcelMetricValidator(var rules: MutableList<ParcelMetricRule>) {
    fun validate(item: ParcelMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: ParcelMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelMetricValidator(): ParcelMetricValidator {
    return ParcelMetricValidator(
        mutableListOf<ParcelMetricRule>(
            ParcelMetricCostRangeRule(9.7, 1462.2),
            ParcelMetricRequiredCodeRule("parcel metric needs this field"),
            ParcelMetricBudgetRangeRule(2.3, 4983.9),
            ParcelMetricRequiredSummaryRule("parcel metric needs an entry"),
        ),
    )
}
