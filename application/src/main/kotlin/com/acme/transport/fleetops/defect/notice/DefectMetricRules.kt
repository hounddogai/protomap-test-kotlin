package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.text.normalize

/** A rule a defect metric must pass. Check returns its problems, or none. */
interface DefectMetricRule {
    fun check(item: DefectMetric): MutableList<String>
}

data class DefectMetricRequiredCountryRule(var message: String) : DefectMetricRule {
    override fun check(item: DefectMetric): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectMetricAllowedNameRule(var allowed: MutableList<String>) : DefectMetricRule {
    override fun check(item: DefectMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class DefectMetricRequiredUnitRule(var message: String) : DefectMetricRule {
    override fun check(item: DefectMetric): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectMetricUnitLengthRule(var maxLength: Int) : DefectMetricRule {
    override fun check(item: DefectMetric): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectMetricCountryLengthRule(var maxLength: Int) : DefectMetricRule {
    override fun check(item: DefectMetric): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect metric. */
data class DefectMetricValidator(var rules: MutableList<DefectMetricRule>) {
    fun validate(item: DefectMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectMetricValidator(): DefectMetricValidator {
    return DefectMetricValidator(
        mutableListOf<DefectMetricRule>(
            DefectMetricRequiredCountryRule("defect metric needs this field"),
            DefectMetricAllowedNameRule(mutableListOf<String>("tiered", "tiered", "regional")),
            DefectMetricRequiredUnitRule("defect metric needs this field"),
            DefectMetricUnitLengthRule(3600),
            DefectMetricCountryLengthRule(255),
        ),
    )
}
