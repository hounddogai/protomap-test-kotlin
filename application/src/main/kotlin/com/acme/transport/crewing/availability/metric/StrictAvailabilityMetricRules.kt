package com.acme.transport.crewing.availability.metric

/** A rule a strict availability metric must pass. Check returns its problems, or none. */
interface StrictAvailabilityMetricRule {
    fun check(item: StrictAvailabilityMetric): MutableList<String>
}

data class StrictAvailabilityMetricRequiredNameRule(var message: String) : StrictAvailabilityMetricRule {
    override fun check(item: StrictAvailabilityMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityMetricRequiredCodeRule(var message: String) : StrictAvailabilityMetricRule {
    override fun check(item: StrictAvailabilityMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityMetricCodePrefixRule(var prefix: String) : StrictAvailabilityMetricRule {
    override fun check(item: StrictAvailabilityMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityMetricRequiredSummaryRule(var message: String) : StrictAvailabilityMetricRule {
    override fun check(item: StrictAvailabilityMetric): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityMetricCodeLengthRule(var maxLength: Int) : StrictAvailabilityMetricRule {
    override fun check(item: StrictAvailabilityMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict availability metric. */
data class StrictAvailabilityMetricValidator(var rules: MutableList<StrictAvailabilityMetricRule>) {
    fun validate(item: StrictAvailabilityMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictAvailabilityMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAvailabilityMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAvailabilityMetricValidator(): StrictAvailabilityMetricValidator {
    return StrictAvailabilityMetricValidator(
        mutableListOf<StrictAvailabilityMetricRule>(
            StrictAvailabilityMetricRequiredNameRule("strict availability metric needs a value"),
            StrictAvailabilityMetricRequiredCodeRule("strict availability metric needs this field"),
            StrictAvailabilityMetricCodePrefixRule("X"),
            StrictAvailabilityMetricRequiredSummaryRule("strict availability metric needs an entry"),
            StrictAvailabilityMetricCodeLengthRule(32),
        ),
    )
}
