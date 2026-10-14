package com.acme.transport.fleetops.utilization.entry

import com.acme.transport.kit.text.normalize

/** A rule a global utilization entry must pass. Check returns its problems, or none. */
interface GlobalUtilizationEntryRule {
    fun check(item: GlobalUtilizationEntry): MutableList<String>
}

data class GlobalUtilizationEntryPalletsRangeRule(var min: Int, var max: Int) : GlobalUtilizationEntryRule {
    override fun check(item: GlobalUtilizationEntry): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationEntryAllowedCarrierRule(
    var allowed: MutableList<String>,
) : GlobalUtilizationEntryRule {
    override fun check(item: GlobalUtilizationEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationEntryCodeLengthRule(var maxLength: Int) : GlobalUtilizationEntryRule {
    override fun check(item: GlobalUtilizationEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global utilization entry. */
data class GlobalUtilizationEntryValidator(var rules: MutableList<GlobalUtilizationEntryRule>) {
    fun validate(item: GlobalUtilizationEntry): MutableList<String> {
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

    fun requireValid(item: GlobalUtilizationEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalUtilizationEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalUtilizationEntryValidator(): GlobalUtilizationEntryValidator {
    return GlobalUtilizationEntryValidator(
        mutableListOf<GlobalUtilizationEntryRule>(
            GlobalUtilizationEntryPalletsRangeRule(120, 1000),
            GlobalUtilizationEntryAllowedCarrierRule(mutableListOf<String>()),
            GlobalUtilizationEntryCodeLengthRule(16),
        ),
    )
}
