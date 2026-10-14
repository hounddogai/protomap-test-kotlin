package com.acme.transport.linehaul.trailer.cycle

/** A rule a manual trailer metric must pass. Check returns its problems, or none. */
interface ManualTrailerMetricRule {
    fun check(item: ManualTrailerMetric): MutableList<String>
}

data class ManualTrailerMetricCodeLengthRule(var maxLength: Int) : ManualTrailerMetricRule {
    override fun check(item: ManualTrailerMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerMetricRequiredNameRule(var message: String) : ManualTrailerMetricRule {
    override fun check(item: ManualTrailerMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerMetricRequiredCodeRule(var message: String) : ManualTrailerMetricRule {
    override fun check(item: ManualTrailerMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerMetricRequiredTimezoneRule(var message: String) : ManualTrailerMetricRule {
    override fun check(item: ManualTrailerMetric): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer metric. */
data class ManualTrailerMetricValidator(var rules: MutableList<ManualTrailerMetricRule>) {
    fun validate(item: ManualTrailerMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTrailerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerMetricValidator(): ManualTrailerMetricValidator {
    return ManualTrailerMetricValidator(
        mutableListOf<ManualTrailerMetricRule>(
            ManualTrailerMetricCodeLengthRule(1000),
            ManualTrailerMetricRequiredNameRule("manual trailer metric needs this field"),
            ManualTrailerMetricRequiredCodeRule("manual trailer metric needs a value"),
            ManualTrailerMetricRequiredTimezoneRule("manual trailer metric needs a value"),
        ),
    )
}
