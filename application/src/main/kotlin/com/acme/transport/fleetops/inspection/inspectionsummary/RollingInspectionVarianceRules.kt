package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.text.normalize

/** A rule a rolling inspection variance must pass. Check returns its problems, or none. */
interface RollingInspectionVarianceRule {
    fun check(item: RollingInspectionVariance): MutableList<String>
}

data class RollingInspectionVarianceAllowedLabelRule(
    var allowed: MutableList<String>,
) : RollingInspectionVarianceRule {
    override fun check(item: RollingInspectionVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionVarianceOdometerRangeRule(
    var min: Int,
    var max: Int,
) : RollingInspectionVarianceRule {
    override fun check(item: RollingInspectionVariance): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionVarianceCountryLengthRule(var maxLength: Int) : RollingInspectionVarianceRule {
    override fun check(item: RollingInspectionVariance): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionVarianceApprovedGuardRule(var limit: Int) : RollingInspectionVarianceRule {
    override fun check(item: RollingInspectionVariance): MutableList<String> {
        if (item.approved && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling inspection variance. */
data class RollingInspectionVarianceValidator(var rules: MutableList<RollingInspectionVarianceRule>) {
    fun validate(item: RollingInspectionVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingInspectionVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingInspectionVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingInspectionVarianceValidator(): RollingInspectionVarianceValidator {
    return RollingInspectionVarianceValidator(
        mutableListOf<RollingInspectionVarianceRule>(
            RollingInspectionVarianceAllowedLabelRule(mutableListOf<String>("batched", "strict")),
            RollingInspectionVarianceOdometerRangeRule(32, 64),
            RollingInspectionVarianceCountryLengthRule(1000),
            RollingInspectionVarianceApprovedGuardRule(80),
        ),
    )
}
