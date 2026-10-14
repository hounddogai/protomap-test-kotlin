package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.text.normalize

/** A rule a global cross dock summary must pass. Check returns its problems, or none. */
interface GlobalCrossDockSummaryRule {
    fun check(item: GlobalCrossDockSummary): MutableList<String>
}

data class GlobalCrossDockSummaryAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalCrossDockSummaryRule {
    override fun check(item: GlobalCrossDockSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockSummaryCodeLengthRule(var maxLength: Int) : GlobalCrossDockSummaryRule {
    override fun check(item: GlobalCrossDockSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockSummaryRequiredCodeRule(var message: String) : GlobalCrossDockSummaryRule {
    override fun check(item: GlobalCrossDockSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global cross dock summary. */
data class GlobalCrossDockSummaryValidator(var rules: MutableList<GlobalCrossDockSummaryRule>) {
    fun validate(item: GlobalCrossDockSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCrossDockSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCrossDockSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCrossDockSummaryValidator(): GlobalCrossDockSummaryValidator {
    return GlobalCrossDockSummaryValidator(
        mutableListOf<GlobalCrossDockSummaryRule>(
            GlobalCrossDockSummaryAllowedCodeRule(mutableListOf<String>("lenient", "tiered")),
            GlobalCrossDockSummaryCodeLengthRule(1000),
            GlobalCrossDockSummaryRequiredCodeRule("global cross dock summary needs a value"),
        ),
    )
}
