package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.kit.text.normalize

/** A rule a service plan assignment must pass. Check returns its problems, or none. */
interface ServicePlanAssignmentRule {
    fun check(item: ServicePlanAssignment): MutableList<String>
}

data class ServicePlanAssignmentCodeLengthRule(var maxLength: Int) : ServicePlanAssignmentRule {
    override fun check(item: ServicePlanAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAssignmentRequiredDescriptionRule(var message: String) : ServicePlanAssignmentRule {
    override fun check(item: ServicePlanAssignment): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAssignmentAttemptsRangeRule(var min: Int, var max: Int) : ServicePlanAssignmentRule {
    override fun check(item: ServicePlanAssignment): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class ServicePlanAssignmentAllowedSegmentRule(
    var allowed: MutableList<String>,
) : ServicePlanAssignmentRule {
    override fun check(item: ServicePlanAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAssignmentRequiredSegmentRule(var message: String) : ServicePlanAssignmentRule {
    override fun check(item: ServicePlanAssignment): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan assignment. */
data class ServicePlanAssignmentValidator(var rules: MutableList<ServicePlanAssignmentRule>) {
    fun validate(item: ServicePlanAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanAssignmentValidator(): ServicePlanAssignmentValidator {
    return ServicePlanAssignmentValidator(
        mutableListOf<ServicePlanAssignmentRule>(
            ServicePlanAssignmentCodeLengthRule(80),
            ServicePlanAssignmentRequiredDescriptionRule("service plan assignment needs an entry"),
            ServicePlanAssignmentAttemptsRangeRule(255, 1000),
            ServicePlanAssignmentAllowedSegmentRule(mutableListOf<String>("capped", "adaptive")),
            ServicePlanAssignmentRequiredSegmentRule("service plan assignment needs an entry"),
        ),
    )
}
