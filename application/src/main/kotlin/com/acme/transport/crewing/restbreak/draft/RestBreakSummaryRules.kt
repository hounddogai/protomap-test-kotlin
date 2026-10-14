package com.acme.transport.crewing.restbreak.draft

/** A rule a rest break summary must pass. Check returns its problems, or none. */
interface RestBreakSummaryRule {
    fun check(item: RestBreakSummary): MutableList<String>
}

data class RestBreakSummaryCurrencyLengthRule(var maxLength: Int) : RestBreakSummaryRule {
    override fun check(item: RestBreakSummary): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakSummaryCodePrefixRule(var prefix: String) : RestBreakSummaryRule {
    override fun check(item: RestBreakSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakSummaryCodeLengthRule(var maxLength: Int) : RestBreakSummaryRule {
    override fun check(item: RestBreakSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakSummaryRequiredSourceRule(var message: String) : RestBreakSummaryRule {
    override fun check(item: RestBreakSummary): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakSummaryAttemptsRangeRule(var min: Int, var max: Int) : RestBreakSummaryRule {
    override fun check(item: RestBreakSummary): MutableList<String> {
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

data class RestBreakSummaryRequiredCurrencyRule(var message: String) : RestBreakSummaryRule {
    override fun check(item: RestBreakSummary): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break summary. */
data class RestBreakSummaryValidator(var rules: MutableList<RestBreakSummaryRule>) {
    fun validate(item: RestBreakSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakSummaryValidator(): RestBreakSummaryValidator {
    return RestBreakSummaryValidator(
        mutableListOf<RestBreakSummaryRule>(
            RestBreakSummaryCurrencyLengthRule(86400),
            RestBreakSummaryCodePrefixRule("Q"),
            RestBreakSummaryCodeLengthRule(255),
            RestBreakSummaryRequiredSourceRule("rest break summary needs an entry"),
            RestBreakSummaryAttemptsRangeRule(16, 3600),
            RestBreakSummaryRequiredCurrencyRule("rest break summary needs an entry"),
        ),
    )
}
