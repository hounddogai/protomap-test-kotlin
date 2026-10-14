package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.text.normalize

/** A rule a scheduled cross dock summary must pass. Check returns its problems, or none. */
interface ScheduledCrossDockSummaryRule {
    fun check(item: ScheduledCrossDockSummary): MutableList<String>
}

data class ScheduledCrossDockSummaryLabelLengthRule(var maxLength: Int) : ScheduledCrossDockSummaryRule {
    override fun check(item: ScheduledCrossDockSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCrossDockSummaryAllowedLocaleRule(
    var allowed: MutableList<String>,
) : ScheduledCrossDockSummaryRule {
    override fun check(item: ScheduledCrossDockSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCrossDockSummaryRequiredCodeRule(var message: String) : ScheduledCrossDockSummaryRule {
    override fun check(item: ScheduledCrossDockSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCrossDockSummaryRequiredLabelRule(var message: String) : ScheduledCrossDockSummaryRule {
    override fun check(item: ScheduledCrossDockSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled cross dock summary. */
data class ScheduledCrossDockSummaryValidator(var rules: MutableList<ScheduledCrossDockSummaryRule>) {
    fun validate(item: ScheduledCrossDockSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledCrossDockSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCrossDockSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCrossDockSummaryValidator(): ScheduledCrossDockSummaryValidator {
    return ScheduledCrossDockSummaryValidator(
        mutableListOf<ScheduledCrossDockSummaryRule>(
            ScheduledCrossDockSummaryLabelLengthRule(64),
            ScheduledCrossDockSummaryAllowedLocaleRule(mutableListOf<String>("legacy", "scheduled")),
            ScheduledCrossDockSummaryRequiredCodeRule("scheduled cross dock summary needs an entry"),
            ScheduledCrossDockSummaryRequiredLabelRule("scheduled cross dock summary needs an entry"),
        ),
    )
}
