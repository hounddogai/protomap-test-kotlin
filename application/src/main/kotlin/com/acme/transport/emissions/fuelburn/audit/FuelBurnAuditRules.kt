package com.acme.transport.emissions.fuelburn.audit

/** A rule a fuel burn audit must pass. Check returns its problems, or none. */
interface FuelBurnAuditRule {
    fun check(item: FuelBurnAudit): MutableList<String>
}

data class FuelBurnAuditRequiredLabelRule(var message: String) : FuelBurnAuditRule {
    override fun check(item: FuelBurnAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAuditRequiredCodeRule(var message: String) : FuelBurnAuditRule {
    override fun check(item: FuelBurnAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAuditStopCountRangeRule(var min: Int, var max: Int) : FuelBurnAuditRule {
    override fun check(item: FuelBurnAudit): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn audit. */
data class FuelBurnAuditValidator(var rules: MutableList<FuelBurnAuditRule>) {
    fun validate(item: FuelBurnAudit): MutableList<String> {
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

    fun requireValid(item: FuelBurnAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnAuditValidator(): FuelBurnAuditValidator {
    return FuelBurnAuditValidator(
        mutableListOf<FuelBurnAuditRule>(
            FuelBurnAuditRequiredLabelRule("fuel burn audit needs this field"),
            FuelBurnAuditRequiredCodeRule("fuel burn audit needs a value"),
            FuelBurnAuditStopCountRangeRule(1000, 3600),
        ),
    )
}
