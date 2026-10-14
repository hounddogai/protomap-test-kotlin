package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.text.normalize

/** A rule a primary violation summary must pass. Check returns its problems, or none. */
interface PrimaryViolationSummaryRule {
    fun check(item: PrimaryViolationSummary): MutableList<String>
}

data class PrimaryViolationSummaryAllowedLabelRule(
    var allowed: MutableList<String>,
) : PrimaryViolationSummaryRule {
    override fun check(item: PrimaryViolationSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryViolationSummaryRequiredCodeRule(var message: String) : PrimaryViolationSummaryRule {
    override fun check(item: PrimaryViolationSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryViolationSummarySourceLengthRule(var maxLength: Int) : PrimaryViolationSummaryRule {
    override fun check(item: PrimaryViolationSummary): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary violation summary. */
data class PrimaryViolationSummaryValidator(var rules: MutableList<PrimaryViolationSummaryRule>) {
    fun validate(item: PrimaryViolationSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryViolationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryViolationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryViolationSummaryValidator(): PrimaryViolationSummaryValidator {
    return PrimaryViolationSummaryValidator(
        mutableListOf<PrimaryViolationSummaryRule>(
            PrimaryViolationSummaryAllowedLabelRule(mutableListOf<String>("manual")),
            PrimaryViolationSummaryRequiredCodeRule("primary violation summary needs a value"),
            PrimaryViolationSummarySourceLengthRule(1000),
        ),
    )
}
