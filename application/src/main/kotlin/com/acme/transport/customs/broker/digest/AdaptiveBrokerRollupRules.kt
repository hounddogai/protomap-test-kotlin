package com.acme.transport.customs.broker.digest

/** A rule a adaptive broker rollup must pass. Check returns its problems, or none. */
interface AdaptiveBrokerRollupRule {
    fun check(item: AdaptiveBrokerRollup): MutableList<String>
}

data class AdaptiveBrokerRollupCreatedAtRangeRule(var min: Int, var max: Int) : AdaptiveBrokerRollupRule {
    override fun check(item: AdaptiveBrokerRollup): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveBrokerRollupCodePrefixRule(var prefix: String) : AdaptiveBrokerRollupRule {
    override fun check(item: AdaptiveBrokerRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerRollupSequenceRangeRule(var min: Int, var max: Int) : AdaptiveBrokerRollupRule {
    override fun check(item: AdaptiveBrokerRollup): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerRollupStartsAtRangeRule(var min: Int, var max: Int) : AdaptiveBrokerRollupRule {
    override fun check(item: AdaptiveBrokerRollup): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveBrokerRollupReviewerLengthRule(var maxLength: Int) : AdaptiveBrokerRollupRule {
    override fun check(item: AdaptiveBrokerRollup): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive broker rollup. */
data class AdaptiveBrokerRollupValidator(var rules: MutableList<AdaptiveBrokerRollupRule>) {
    fun validate(item: AdaptiveBrokerRollup): MutableList<String> {
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

    fun requireValid(item: AdaptiveBrokerRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveBrokerRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveBrokerRollupValidator(): AdaptiveBrokerRollupValidator {
    return AdaptiveBrokerRollupValidator(
        mutableListOf<AdaptiveBrokerRollupRule>(
            AdaptiveBrokerRollupCreatedAtRangeRule(32, 255),
            AdaptiveBrokerRollupCodePrefixRule("OPS"),
            AdaptiveBrokerRollupSequenceRangeRule(64, 120),
            AdaptiveBrokerRollupStartsAtRangeRule(255, 1000),
            AdaptiveBrokerRollupReviewerLengthRule(3600),
        ),
    )
}
