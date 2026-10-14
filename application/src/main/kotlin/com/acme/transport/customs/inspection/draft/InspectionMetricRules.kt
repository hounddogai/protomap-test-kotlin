package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.text.normalize

/** A rule a inspection metric must pass. Check returns its problems, or none. */
interface InspectionMetricRule {
    fun check(item: InspectionMetric): MutableList<String>
}

data class InspectionMetricCodePrefixRule(var prefix: String) : InspectionMetricRule {
    override fun check(item: InspectionMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionMetricPinnedGuardRule(var limit: Int) : InspectionMetricRule {
    override fun check(item: InspectionMetric): MutableList<String> {
        if (item.pinned && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class InspectionMetricRequiredCodeRule(var message: String) : InspectionMetricRule {
    override fun check(item: InspectionMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionMetricAmountRangeRule(var min: Double, var max: Double) : InspectionMetricRule {
    override fun check(item: InspectionMetric): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionMetricRequiredCountryRule(var message: String) : InspectionMetricRule {
    override fun check(item: InspectionMetric): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionMetricAllowedCodeRule(var allowed: MutableList<String>) : InspectionMetricRule {
    override fun check(item: InspectionMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
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
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
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
            InspectionMetricCodePrefixRule("REF"),
            InspectionMetricPinnedGuardRule(16),
            InspectionMetricRequiredCodeRule("inspection metric needs this field"),
            InspectionMetricAmountRangeRule(7.3, 2863.8),
            InspectionMetricRequiredCountryRule("inspection metric needs a value"),
            InspectionMetricAllowedCodeRule(mutableListOf<String>("default", "regional", "primary")),
        ),
    )
}
