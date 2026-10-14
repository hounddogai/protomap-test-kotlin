package com.acme.transport.fleetops.serviceplan.slot

import com.acme.transport.kit.text.normalize

/** A rule a tiered service plan assignment must pass. Check returns its problems, or none. */
interface TieredServicePlanAssignmentRule {
    fun check(item: TieredServicePlanAssignment): MutableList<String>
}

data class TieredServicePlanAssignmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : TieredServicePlanAssignmentRule {
    override fun check(item: TieredServicePlanAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class TieredServicePlanAssignmentFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : TieredServicePlanAssignmentRule {
    override fun check(item: TieredServicePlanAssignment): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredServicePlanAssignmentMaximumRangeRule(
    var min: Double,
    var max: Double,
) : TieredServicePlanAssignmentRule {
    override fun check(item: TieredServicePlanAssignment): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredServicePlanAssignmentEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : TieredServicePlanAssignmentRule {
    override fun check(item: TieredServicePlanAssignment): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered service plan assignment. */
data class TieredServicePlanAssignmentValidator(var rules: MutableList<TieredServicePlanAssignmentRule>) {
    fun validate(item: TieredServicePlanAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredServicePlanAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredServicePlanAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredServicePlanAssignmentValidator(): TieredServicePlanAssignmentValidator {
    return TieredServicePlanAssignmentValidator(
        mutableListOf<TieredServicePlanAssignmentRule>(
            TieredServicePlanAssignmentAllowedCodeRule(mutableListOf<String>("default", "manual", "strict")),
            TieredServicePlanAssignmentFloorValueRangeRule(7.0, 2433.8),
            TieredServicePlanAssignmentMaximumRangeRule(9.7, 2585.9),
            TieredServicePlanAssignmentEtaMinutesRangeRule(64, 1000),
        ),
    )
}
