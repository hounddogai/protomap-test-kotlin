package com.acme.transport.customs.broker.metric

/** A rule a regional broker metric must pass. Check returns its problems, or none. */
interface RegionalBrokerMetricRule {
    fun check(item: RegionalBrokerMetric): MutableList<String>
}

data class RegionalBrokerMetricMinimumRangeRule(var min: Double, var max: Double) : RegionalBrokerMetricRule {
    override fun check(item: RegionalBrokerMetric): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class RegionalBrokerMetricRequiredNotesRule(var message: String) : RegionalBrokerMetricRule {
    override fun check(item: RegionalBrokerMetric): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerMetricRequiredCodeRule(var message: String) : RegionalBrokerMetricRule {
    override fun check(item: RegionalBrokerMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerMetricRequiredCategoryRule(var message: String) : RegionalBrokerMetricRule {
    override fun check(item: RegionalBrokerMetric): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalBrokerMetricNameLengthRule(var maxLength: Int) : RegionalBrokerMetricRule {
    override fun check(item: RegionalBrokerMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional broker metric. */
data class RegionalBrokerMetricValidator(var rules: MutableList<RegionalBrokerMetricRule>) {
    fun validate(item: RegionalBrokerMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalBrokerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalBrokerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalBrokerMetricValidator(): RegionalBrokerMetricValidator {
    return RegionalBrokerMetricValidator(
        mutableListOf<RegionalBrokerMetricRule>(
            RegionalBrokerMetricMinimumRangeRule(9.1, 902.3),
            RegionalBrokerMetricRequiredNotesRule("regional broker metric needs a value"),
            RegionalBrokerMetricRequiredCodeRule("regional broker metric needs a value"),
            RegionalBrokerMetricRequiredCategoryRule("regional broker metric needs this field"),
            RegionalBrokerMetricNameLengthRule(3600),
        ),
    )
}
