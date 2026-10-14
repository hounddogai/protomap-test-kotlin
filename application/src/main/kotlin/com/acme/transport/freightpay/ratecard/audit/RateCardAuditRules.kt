package com.acme.transport.freightpay.ratecard.audit

import com.acme.transport.kit.text.normalize

/** A rule a rate card audit must pass. Check returns its problems, or none. */
interface RateCardAuditRule {
    fun check(item: RateCardAudit): MutableList<String>
}

data class RateCardAuditRequiredUnitRule(var message: String) : RateCardAuditRule {
    override fun check(item: RateCardAudit): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardAuditRequiredVehicleIdRule(var message: String) : RateCardAuditRule {
    override fun check(item: RateCardAudit): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardAuditAllowedVehicleIdRule(var allowed: MutableList<String>) : RateCardAuditRule {
    override fun check(item: RateCardAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card audit. */
data class RateCardAuditValidator(var rules: MutableList<RateCardAuditRule>) {
    fun validate(item: RateCardAudit): MutableList<String> {
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

    fun requireValid(item: RateCardAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardAuditValidator(): RateCardAuditValidator {
    return RateCardAuditValidator(
        mutableListOf<RateCardAuditRule>(
            RateCardAuditRequiredUnitRule("rate card audit needs a value"),
            RateCardAuditRequiredVehicleIdRule("rate card audit needs this field"),
            RateCardAuditAllowedVehicleIdRule(mutableListOf<String>("lenient", "weighted")),
        ),
    )
}
