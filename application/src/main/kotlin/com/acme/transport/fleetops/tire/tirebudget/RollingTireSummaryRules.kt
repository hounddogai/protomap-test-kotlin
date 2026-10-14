package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.text.normalize

/** A rule a rolling tire summary must pass. Check returns its problems, or none. */
interface RollingTireSummaryRule {
    fun check(item: RollingTireSummary): MutableList<String>
}

data class RollingTireSummaryAllowedCurrencyRule(var allowed: MutableList<String>) : RollingTireSummaryRule {
    override fun check(item: RollingTireSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RollingTireSummaryVehicleIdLengthRule(var maxLength: Int) : RollingTireSummaryRule {
    override fun check(item: RollingTireSummary): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTireSummaryCodeLengthRule(var maxLength: Int) : RollingTireSummaryRule {
    override fun check(item: RollingTireSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling tire summary. */
data class RollingTireSummaryValidator(var rules: MutableList<RollingTireSummaryRule>) {
    fun validate(item: RollingTireSummary): MutableList<String> {
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

    fun requireValid(item: RollingTireSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTireSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTireSummaryValidator(): RollingTireSummaryValidator {
    return RollingTireSummaryValidator(
        mutableListOf<RollingTireSummaryRule>(
            RollingTireSummaryAllowedCurrencyRule(mutableListOf<String>("weighted")),
            RollingTireSummaryVehicleIdLengthRule(16),
            RollingTireSummaryCodeLengthRule(86400),
        ),
    )
}
