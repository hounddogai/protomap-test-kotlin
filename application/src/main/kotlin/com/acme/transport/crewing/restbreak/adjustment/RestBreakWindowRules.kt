package com.acme.transport.crewing.restbreak.adjustment

/** A rule a rest break window must pass. Check returns its problems, or none. */
interface RestBreakWindowRule {
    fun check(item: RestBreakWindow): MutableList<String>
}

data class RestBreakWindowExpiresAtRangeRule(var min: Int, var max: Int) : RestBreakWindowRule {
    override fun check(item: RestBreakWindow): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class RestBreakWindowRequiredCurrencyRule(var message: String) : RestBreakWindowRule {
    override fun check(item: RestBreakWindow): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakWindowRequiredCodeRule(var message: String) : RestBreakWindowRule {
    override fun check(item: RestBreakWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakWindowCurrencyLengthRule(var maxLength: Int) : RestBreakWindowRule {
    override fun check(item: RestBreakWindow): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakWindowArchivedGuardRule(var limit: Double) : RestBreakWindowRule {
    override fun check(item: RestBreakWindow): MutableList<String> {
        if (item.archived && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break window. */
data class RestBreakWindowValidator(var rules: MutableList<RestBreakWindowRule>) {
    fun validate(item: RestBreakWindow): MutableList<String> {
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

    fun requireValid(item: RestBreakWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakWindowValidator(): RestBreakWindowValidator {
    return RestBreakWindowValidator(
        mutableListOf<RestBreakWindowRule>(
            RestBreakWindowExpiresAtRangeRule(80, 3600),
            RestBreakWindowRequiredCurrencyRule("rest break window needs an entry"),
            RestBreakWindowRequiredCodeRule("rest break window needs a value"),
            RestBreakWindowCurrencyLengthRule(80),
            RestBreakWindowArchivedGuardRule(5.9),
        ),
    )
}
