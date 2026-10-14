package com.acme.transport.safety.violation.metric

/** A rule a manual violation metric must pass. Check returns its problems, or none. */
interface ManualViolationMetricRule {
    fun check(item: ManualViolationMetric): MutableList<String>
}

data class ManualViolationMetricLabelLengthRule(var maxLength: Int) : ManualViolationMetricRule {
    override fun check(item: ManualViolationMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationMetricCodeLengthRule(var maxLength: Int) : ManualViolationMetricRule {
    override fun check(item: ManualViolationMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationMetricRequiredLabelRule(var message: String) : ManualViolationMetricRule {
    override fun check(item: ManualViolationMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationMetricMarginRangeRule(var min: Double, var max: Double) : ManualViolationMetricRule {
    override fun check(item: ManualViolationMetric): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual violation metric. */
data class ManualViolationMetricValidator(var rules: MutableList<ManualViolationMetricRule>) {
    fun validate(item: ManualViolationMetric): MutableList<String> {
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

    fun requireValid(item: ManualViolationMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualViolationMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultManualViolationMetricValidator(): ManualViolationMetricValidator {
    return ManualViolationMetricValidator(
        mutableListOf<ManualViolationMetricRule>(
            ManualViolationMetricLabelLengthRule(64),
            ManualViolationMetricCodeLengthRule(255),
            ManualViolationMetricRequiredLabelRule("manual violation metric needs an entry"),
            ManualViolationMetricMarginRangeRule(8.9, 219.7),
        ),
    )
}
