package com.acme.transport.crewing.restbreak.claim

/** A rule a adaptive rest break exception must pass. Check returns its problems, or none. */
interface AdaptiveRestBreakExceptionRule {
    fun check(item: AdaptiveRestBreakException): MutableList<String>
}

data class AdaptiveRestBreakExceptionGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveRestBreakExceptionRule {
    override fun check(item: AdaptiveRestBreakException): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class AdaptiveRestBreakExceptionCodePrefixRule(var prefix: String) : AdaptiveRestBreakExceptionRule {
    override fun check(item: AdaptiveRestBreakException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRestBreakExceptionRequiredChecksumRule(
    var message: String,
) : AdaptiveRestBreakExceptionRule {
    override fun check(item: AdaptiveRestBreakException): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive rest break exception. */
data class AdaptiveRestBreakExceptionValidator(var rules: MutableList<AdaptiveRestBreakExceptionRule>) {
    fun validate(item: AdaptiveRestBreakException): MutableList<String> {
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

    fun requireValid(item: AdaptiveRestBreakException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRestBreakException>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRestBreakExceptionValidator(): AdaptiveRestBreakExceptionValidator {
    return AdaptiveRestBreakExceptionValidator(
        mutableListOf<AdaptiveRestBreakExceptionRule>(
            AdaptiveRestBreakExceptionGraceDaysRangeRule(120, 1000),
            AdaptiveRestBreakExceptionCodePrefixRule("REF"),
            AdaptiveRestBreakExceptionRequiredChecksumRule("adaptive rest break exception needs an entry"),
        ),
    )
}
