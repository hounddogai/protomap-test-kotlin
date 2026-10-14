package com.acme.transport.customs.clearance.reservation

import com.acme.transport.kit.text.normalize

/** A rule a clearance summary must pass. Check returns its problems, or none. */
interface ClearanceSummaryRule {
    fun check(item: ClearanceSummary): MutableList<String>
}

data class ClearanceSummaryCodePrefixRule(var prefix: String) : ClearanceSummaryRule {
    override fun check(item: ClearanceSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceSummaryAttemptsRangeRule(var min: Int, var max: Int) : ClearanceSummaryRule {
    override fun check(item: ClearanceSummary): MutableList<String> {
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

data class ClearanceSummaryRequiredDescriptionRule(var message: String) : ClearanceSummaryRule {
    override fun check(item: ClearanceSummary): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceSummaryAllowedNameRule(var allowed: MutableList<String>) : ClearanceSummaryRule {
    override fun check(item: ClearanceSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceSummaryRequiredDockRule(var message: String) : ClearanceSummaryRule {
    override fun check(item: ClearanceSummary): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance summary. */
data class ClearanceSummaryValidator(var rules: MutableList<ClearanceSummaryRule>) {
    fun validate(item: ClearanceSummary): MutableList<String> {
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

    fun requireValid(item: ClearanceSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceSummaryValidator(): ClearanceSummaryValidator {
    return ClearanceSummaryValidator(
        mutableListOf<ClearanceSummaryRule>(
            ClearanceSummaryCodePrefixRule("ACM"),
            ClearanceSummaryAttemptsRangeRule(64, 86400),
            ClearanceSummaryRequiredDescriptionRule("clearance summary needs this field"),
            ClearanceSummaryAllowedNameRule(mutableListOf<String>()),
            ClearanceSummaryRequiredDockRule("clearance summary needs a value"),
        ),
    )
}
