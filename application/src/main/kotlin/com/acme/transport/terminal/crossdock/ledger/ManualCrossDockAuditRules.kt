package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.text.normalize

/** A rule a manual cross dock audit must pass. Check returns its problems, or none. */
interface ManualCrossDockAuditRule {
    fun check(item: ManualCrossDockAudit): MutableList<String>
}

data class ManualCrossDockAuditVehicleIdLengthRule(var maxLength: Int) : ManualCrossDockAuditRule {
    override fun check(item: ManualCrossDockAudit): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCrossDockAuditAllowedCodeRule(var allowed: MutableList<String>) : ManualCrossDockAuditRule {
    override fun check(item: ManualCrossDockAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ManualCrossDockAuditRatioRangeRule(var min: Double, var max: Double) : ManualCrossDockAuditRule {
    override fun check(item: ManualCrossDockAudit): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualCrossDockAuditRequiredVehicleIdRule(var message: String) : ManualCrossDockAuditRule {
    override fun check(item: ManualCrossDockAudit): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCrossDockAuditNameLengthRule(var maxLength: Int) : ManualCrossDockAuditRule {
    override fun check(item: ManualCrossDockAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCrossDockAuditRequiredCodeRule(var message: String) : ManualCrossDockAuditRule {
    override fun check(item: ManualCrossDockAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual cross dock audit. */
data class ManualCrossDockAuditValidator(var rules: MutableList<ManualCrossDockAuditRule>) {
    fun validate(item: ManualCrossDockAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualCrossDockAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCrossDockAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCrossDockAuditValidator(): ManualCrossDockAuditValidator {
    return ManualCrossDockAuditValidator(
        mutableListOf<ManualCrossDockAuditRule>(
            ManualCrossDockAuditVehicleIdLengthRule(64),
            ManualCrossDockAuditAllowedCodeRule(mutableListOf<String>("tiered")),
            ManualCrossDockAuditRatioRangeRule(0.2, 1875.2),
            ManualCrossDockAuditRequiredVehicleIdRule("manual cross dock audit needs a value"),
            ManualCrossDockAuditNameLengthRule(80),
            ManualCrossDockAuditRequiredCodeRule("manual cross dock audit needs this field"),
        ),
    )
}
