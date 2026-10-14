package com.acme.transport.safety.scorecard.threshold

/** A rule a scorecard threshold must pass. Check returns its problems, or none. */
interface ScorecardThresholdRule {
    fun check(item: ScorecardThreshold): MutableList<String>
}

data class ScorecardThresholdMaximumRangeRule(var min: Double, var max: Double) : ScorecardThresholdRule {
    override fun check(item: ScorecardThreshold): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class ScorecardThresholdAttemptsRangeRule(var min: Int, var max: Int) : ScorecardThresholdRule {
    override fun check(item: ScorecardThreshold): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardThresholdRequiredCodeRule(var message: String) : ScorecardThresholdRule {
    override fun check(item: ScorecardThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardThresholdCodeLengthRule(var maxLength: Int) : ScorecardThresholdRule {
    override fun check(item: ScorecardThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardThresholdCurrencyLengthRule(var maxLength: Int) : ScorecardThresholdRule {
    override fun check(item: ScorecardThreshold): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard threshold. */
data class ScorecardThresholdValidator(var rules: MutableList<ScorecardThresholdRule>) {
    fun validate(item: ScorecardThreshold): MutableList<String> {
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

    fun requireValid(item: ScorecardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardThresholdValidator(): ScorecardThresholdValidator {
    return ScorecardThresholdValidator(
        mutableListOf<ScorecardThresholdRule>(
            ScorecardThresholdMaximumRangeRule(0.9, 1211.4),
            ScorecardThresholdAttemptsRangeRule(32, 86400),
            ScorecardThresholdRequiredCodeRule("scorecard threshold needs an entry"),
            ScorecardThresholdCodeLengthRule(255),
            ScorecardThresholdCurrencyLengthRule(86400),
        ),
    )
}
