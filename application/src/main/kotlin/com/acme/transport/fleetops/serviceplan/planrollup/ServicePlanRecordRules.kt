package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.text.normalize

/** A rule a service plan record must pass. Check returns its problems, or none. */
interface ServicePlanRecordRule {
    fun check(item: ServicePlanRecord): MutableList<String>
}

data class ServicePlanRecordRequiredCodeRule(var message: String) : ServicePlanRecordRule {
    override fun check(item: ServicePlanRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanRecordRequiredNameRule(var message: String) : ServicePlanRecordRule {
    override fun check(item: ServicePlanRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanRecordCapacityRangeRule(var min: Int, var max: Int) : ServicePlanRecordRule {
    override fun check(item: ServicePlanRecord): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanRecordSegmentLengthRule(var maxLength: Int) : ServicePlanRecordRule {
    override fun check(item: ServicePlanRecord): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanRecordAllowedCodeRule(var allowed: MutableList<String>) : ServicePlanRecordRule {
    override fun check(item: ServicePlanRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan record. */
data class ServicePlanRecordValidator(var rules: MutableList<ServicePlanRecordRule>) {
    fun validate(item: ServicePlanRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanRecordValidator(): ServicePlanRecordValidator {
    return ServicePlanRecordValidator(
        mutableListOf<ServicePlanRecordRule>(
            ServicePlanRecordRequiredCodeRule("service plan record needs this field"),
            ServicePlanRecordRequiredNameRule("service plan record needs an entry"),
            ServicePlanRecordCapacityRangeRule(80, 120),
            ServicePlanRecordSegmentLengthRule(16),
            ServicePlanRecordAllowedCodeRule(mutableListOf<String>("batched")),
        ),
    )
}
