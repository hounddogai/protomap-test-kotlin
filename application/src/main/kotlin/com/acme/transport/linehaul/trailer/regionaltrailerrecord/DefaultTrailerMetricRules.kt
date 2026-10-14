package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.text.normalize

/** A rule a default trailer metric must pass. Check returns its problems, or none. */
interface DefaultTrailerMetricRule {
    fun check(item: DefaultTrailerMetric): MutableList<String>
}

data class DefaultTrailerMetricRequiredDockRule(var message: String) : DefaultTrailerMetricRule {
    override fun check(item: DefaultTrailerMetric): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerMetricDescriptionLengthRule(var maxLength: Int) : DefaultTrailerMetricRule {
    override fun check(item: DefaultTrailerMetric): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerMetricAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : DefaultTrailerMetricRule {
    override fun check(item: DefaultTrailerMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default trailer metric. */
data class DefaultTrailerMetricValidator(var rules: MutableList<DefaultTrailerMetricRule>) {
    fun validate(item: DefaultTrailerMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTrailerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTrailerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTrailerMetricValidator(): DefaultTrailerMetricValidator {
    return DefaultTrailerMetricValidator(
        mutableListOf<DefaultTrailerMetricRule>(
            DefaultTrailerMetricRequiredDockRule("default trailer metric needs a value"),
            DefaultTrailerMetricDescriptionLengthRule(64),
            DefaultTrailerMetricAllowedDescriptionRule(mutableListOf<String>("global", "lenient", "primary")),
        ),
    )
}
