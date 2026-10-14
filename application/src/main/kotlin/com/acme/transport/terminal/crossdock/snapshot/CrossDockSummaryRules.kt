package com.acme.transport.terminal.crossdock.snapshot

/** A rule a cross dock summary must pass. Check returns its problems, or none. */
interface CrossDockSummaryRule {
    fun check(item: CrossDockSummary): MutableList<String>
}

data class CrossDockSummaryCurrencyLengthRule(var maxLength: Int) : CrossDockSummaryRule {
    override fun check(item: CrossDockSummary): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockSummaryRequiredCodeRule(var message: String) : CrossDockSummaryRule {
    override fun check(item: CrossDockSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockSummaryCodeLengthRule(var maxLength: Int) : CrossDockSummaryRule {
    override fun check(item: CrossDockSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock summary. */
data class CrossDockSummaryValidator(var rules: MutableList<CrossDockSummaryRule>) {
    fun validate(item: CrossDockSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockSummaryValidator(): CrossDockSummaryValidator {
    return CrossDockSummaryValidator(
        mutableListOf<CrossDockSummaryRule>(
            CrossDockSummaryCurrencyLengthRule(32),
            CrossDockSummaryRequiredCodeRule("cross dock summary needs an entry"),
            CrossDockSummaryCodeLengthRule(32),
        ),
    )
}
