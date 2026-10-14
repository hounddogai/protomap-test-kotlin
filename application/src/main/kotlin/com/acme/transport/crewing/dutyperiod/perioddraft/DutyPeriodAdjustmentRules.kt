package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.text.normalize

/** A rule a duty period adjustment must pass. Check returns its problems, or none. */
interface DutyPeriodAdjustmentRule {
    fun check(item: DutyPeriodAdjustment): MutableList<String>
}

data class DutyPeriodAdjustmentCodePrefixRule(var prefix: String) : DutyPeriodAdjustmentRule {
    override fun check(item: DutyPeriodAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAdjustmentAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : DutyPeriodAdjustmentRule {
    override fun check(item: DutyPeriodAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAdjustmentRequiredVehicleIdRule(var message: String) : DutyPeriodAdjustmentRule {
    override fun check(item: DutyPeriodAdjustment): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period adjustment. */
data class DutyPeriodAdjustmentValidator(var rules: MutableList<DutyPeriodAdjustmentRule>) {
    fun validate(item: DutyPeriodAdjustment): MutableList<String> {
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

    fun requireValid(item: DutyPeriodAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodAdjustmentValidator(): DutyPeriodAdjustmentValidator {
    return DutyPeriodAdjustmentValidator(
        mutableListOf<DutyPeriodAdjustmentRule>(
            DutyPeriodAdjustmentCodePrefixRule("OPS"),
            DutyPeriodAdjustmentAllowedVehicleIdRule(mutableListOf<String>()),
            DutyPeriodAdjustmentRequiredVehicleIdRule("duty period adjustment needs an entry"),
        ),
    )
}
