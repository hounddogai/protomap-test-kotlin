package com.acme.transport.freightpay.audit.plan

import com.acme.transport.kit.text.normalize

/** A rule a audit plan must pass. Check returns its problems, or none. */
interface AuditPlanRule {
    fun check(item: AuditPlan): MutableList<String>
}

data class AuditPlanVehicleIdLengthRule(var maxLength: Int) : AuditPlanRule {
    override fun check(item: AuditPlan): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditPlanCapacityRangeRule(var min: Int, var max: Int) : AuditPlanRule {
    override fun check(item: AuditPlan): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class AuditPlanAllowedVehicleIdRule(var allowed: MutableList<String>) : AuditPlanRule {
    override fun check(item: AuditPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AuditPlanRequiredVehicleIdRule(var message: String) : AuditPlanRule {
    override fun check(item: AuditPlan): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit plan. */
data class AuditPlanValidator(var rules: MutableList<AuditPlanRule>) {
    fun validate(item: AuditPlan): MutableList<String> {
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

    fun requireValid(item: AuditPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditPlanValidator(): AuditPlanValidator {
    return AuditPlanValidator(
        mutableListOf<AuditPlanRule>(
            AuditPlanVehicleIdLengthRule(80),
            AuditPlanCapacityRangeRule(32, 255),
            AuditPlanAllowedVehicleIdRule(mutableListOf<String>("fallback", "manual")),
            AuditPlanRequiredVehicleIdRule("audit plan needs an entry"),
        ),
    )
}
