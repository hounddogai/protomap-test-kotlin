package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.normalize

/** A rule a inspection metric must pass. Check returns its problems, or none. */
interface InspectionMetricRule {
    fun check(item: InspectionMetric): MutableList<String>
}

data class InspectionMetricMaximumRangeRule(var min: Double, var max: Double) : InspectionMetricRule {
    override fun check(item: InspectionMetric): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionMetricRequiredNameRule(var message: String) : InspectionMetricRule {
    override fun check(item: InspectionMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionMetricCodeLengthRule(var maxLength: Int) : InspectionMetricRule {
    override fun check(item: InspectionMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionMetricAllowedNotesRule(var allowed: MutableList<String>) : InspectionMetricRule {
    override fun check(item: InspectionMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection metric. */
data class InspectionMetricValidator(var rules: MutableList<InspectionMetricRule>) {
    fun validate(item: InspectionMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionMetricValidator(): InspectionMetricValidator {
    return InspectionMetricValidator(
        mutableListOf<InspectionMetricRule>(
            InspectionMetricMaximumRangeRule(7.0, 4193.2),
            InspectionMetricRequiredNameRule("inspection metric needs an entry"),
            InspectionMetricCodeLengthRule(80),
            InspectionMetricAllowedNotesRule(mutableListOf<String>("regional", "global", "regional")),
        ),
    )
}
