package com.acme.transport.fleetops.tire.summary

/** A rule a tire summary must pass. Check returns its problems, or none. */
interface TireSummaryRule {
    fun check(item: TireSummary): MutableList<String>
}

data class TireSummaryCodePrefixRule(var prefix: String) : TireSummaryRule {
    override fun check(item: TireSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireSummaryCodeLengthRule(var maxLength: Int) : TireSummaryRule {
    override fun check(item: TireSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireSummarySequenceRangeRule(var min: Int, var max: Int) : TireSummaryRule {
    override fun check(item: TireSummary): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireSummaryRequiredCodeRule(var message: String) : TireSummaryRule {
    override fun check(item: TireSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire summary. */
data class TireSummaryValidator(var rules: MutableList<TireSummaryRule>) {
    fun validate(item: TireSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultTireSummaryValidator(): TireSummaryValidator {
    return TireSummaryValidator(
        mutableListOf<TireSummaryRule>(
            TireSummaryCodePrefixRule("X"),
            TireSummaryCodeLengthRule(1000),
            TireSummarySequenceRangeRule(3600, 86400),
            TireSummaryRequiredCodeRule("tire summary needs an entry"),
        ),
    )
}
