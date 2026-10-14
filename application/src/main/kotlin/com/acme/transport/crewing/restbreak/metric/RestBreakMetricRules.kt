package com.acme.transport.crewing.restbreak.metric

/** A rule a rest break metric must pass. Check returns its problems, or none. */
interface RestBreakMetricRule {
    fun check(item: RestBreakMetric): MutableList<String>
}

data class RestBreakMetricNameLengthRule(var maxLength: Int) : RestBreakMetricRule {
    override fun check(item: RestBreakMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakMetricTimezoneLengthRule(var maxLength: Int) : RestBreakMetricRule {
    override fun check(item: RestBreakMetric): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakMetricRequiredNameRule(var message: String) : RestBreakMetricRule {
    override fun check(item: RestBreakMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break metric. */
data class RestBreakMetricValidator(var rules: MutableList<RestBreakMetricRule>) {
    fun validate(item: RestBreakMetric): MutableList<String> {
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

    fun requireValid(item: RestBreakMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakMetricValidator(): RestBreakMetricValidator {
    return RestBreakMetricValidator(
        mutableListOf<RestBreakMetricRule>(
            RestBreakMetricNameLengthRule(64),
            RestBreakMetricTimezoneLengthRule(86400),
            RestBreakMetricRequiredNameRule("rest break metric needs this field"),
        ),
    )
}
