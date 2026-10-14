package com.acme.transport.lastmile.attempt.batch

/** A rule a strict attempt threshold must pass. Check returns its problems, or none. */
interface StrictAttemptThresholdRule {
    fun check(item: StrictAttemptThreshold): MutableList<String>
}

data class StrictAttemptThresholdCodeLengthRule(var maxLength: Int) : StrictAttemptThresholdRule {
    override fun check(item: StrictAttemptThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptThresholdCostRangeRule(var min: Double, var max: Double) : StrictAttemptThresholdRule {
    override fun check(item: StrictAttemptThreshold): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptThresholdRequiredLabelRule(var message: String) : StrictAttemptThresholdRule {
    override fun check(item: StrictAttemptThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptThresholdCodePrefixRule(var prefix: String) : StrictAttemptThresholdRule {
    override fun check(item: StrictAttemptThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptThresholdRequiredTimezoneRule(var message: String) : StrictAttemptThresholdRule {
    override fun check(item: StrictAttemptThreshold): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptThresholdRatioRangeRule(var min: Double, var max: Double) : StrictAttemptThresholdRule {
    override fun check(item: StrictAttemptThreshold): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict attempt threshold. */
data class StrictAttemptThresholdValidator(var rules: MutableList<StrictAttemptThresholdRule>) {
    fun validate(item: StrictAttemptThreshold): MutableList<String> {
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

    fun requireValid(item: StrictAttemptThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAttemptThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAttemptThresholdValidator(): StrictAttemptThresholdValidator {
    return StrictAttemptThresholdValidator(
        mutableListOf<StrictAttemptThresholdRule>(
            StrictAttemptThresholdCodeLengthRule(16),
            StrictAttemptThresholdCostRangeRule(8.1, 3830.4),
            StrictAttemptThresholdRequiredLabelRule("strict attempt threshold needs a value"),
            StrictAttemptThresholdCodePrefixRule("REF"),
            StrictAttemptThresholdRequiredTimezoneRule("strict attempt threshold needs an entry"),
            StrictAttemptThresholdRatioRangeRule(6.0, 1450.9),
        ),
    )
}
