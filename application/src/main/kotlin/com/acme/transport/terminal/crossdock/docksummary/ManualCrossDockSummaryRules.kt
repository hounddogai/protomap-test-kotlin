package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.text.normalize

/** A rule a manual cross dock summary must pass. Check returns its problems, or none. */
interface ManualCrossDockSummaryRule {
    fun check(item: ManualCrossDockSummary): MutableList<String>
}

data class ManualCrossDockSummaryAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : ManualCrossDockSummaryRule {
    override fun check(item: ManualCrossDockSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ManualCrossDockSummaryRequiredExternalRefRule(var message: String) : ManualCrossDockSummaryRule {
    override fun check(item: ManualCrossDockSummary): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCrossDockSummaryRetriesRangeRule(var min: Int, var max: Int) : ManualCrossDockSummaryRule {
    override fun check(item: ManualCrossDockSummary): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual cross dock summary. */
data class ManualCrossDockSummaryValidator(var rules: MutableList<ManualCrossDockSummaryRule>) {
    fun validate(item: ManualCrossDockSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualCrossDockSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCrossDockSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCrossDockSummaryValidator(): ManualCrossDockSummaryValidator {
    return ManualCrossDockSummaryValidator(
        mutableListOf<ManualCrossDockSummaryRule>(
            ManualCrossDockSummaryAllowedTimezoneRule(mutableListOf<String>()),
            ManualCrossDockSummaryRequiredExternalRefRule("manual cross dock summary needs an entry"),
            ManualCrossDockSummaryRetriesRangeRule(86400, 86400),
        ),
    )
}
