package com.acme.transport.lastmile.attempt.metric

/** A rule a tiered attempt request must pass. Check returns its problems, or none. */
interface TieredAttemptRequestRule {
    fun check(item: TieredAttemptRequest): MutableList<String>
}

data class TieredAttemptRequestCodePrefixRule(var prefix: String) : TieredAttemptRequestRule {
    override fun check(item: TieredAttemptRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredAttemptRequestMinimumRangeRule(var min: Double, var max: Double) : TieredAttemptRequestRule {
    override fun check(item: TieredAttemptRequest): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class TieredAttemptRequestStopCountRangeRule(var min: Int, var max: Int) : TieredAttemptRequestRule {
    override fun check(item: TieredAttemptRequest): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered attempt request. */
data class TieredAttemptRequestValidator(var rules: MutableList<TieredAttemptRequestRule>) {
    fun validate(item: TieredAttemptRequest): MutableList<String> {
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

    fun requireValid(item: TieredAttemptRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAttemptRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAttemptRequestValidator(): TieredAttemptRequestValidator {
    return TieredAttemptRequestValidator(
        mutableListOf<TieredAttemptRequestRule>(
            TieredAttemptRequestCodePrefixRule("ACM"),
            TieredAttemptRequestMinimumRangeRule(9.7, 4083.3),
            TieredAttemptRequestStopCountRangeRule(32, 120),
        ),
    )
}
