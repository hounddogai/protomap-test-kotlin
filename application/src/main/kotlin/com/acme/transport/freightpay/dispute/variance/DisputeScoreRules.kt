package com.acme.transport.freightpay.dispute.variance

/** A rule a dispute score must pass. Check returns its problems, or none. */
interface DisputeScoreRule {
    fun check(item: DisputeScore): MutableList<String>
}

data class DisputeScoreTimezoneLengthRule(var maxLength: Int) : DisputeScoreRule {
    override fun check(item: DisputeScore): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeScoreRequiredCodeRule(var message: String) : DisputeScoreRule {
    override fun check(item: DisputeScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeScoreCodePrefixRule(var prefix: String) : DisputeScoreRule {
    override fun check(item: DisputeScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeScoreQuantityRangeRule(var min: Int, var max: Int) : DisputeScoreRule {
    override fun check(item: DisputeScore): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class DisputeScoreCreatedAtRangeRule(var min: Int, var max: Int) : DisputeScoreRule {
    override fun check(item: DisputeScore): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute score. */
data class DisputeScoreValidator(var rules: MutableList<DisputeScoreRule>) {
    fun validate(item: DisputeScore): MutableList<String> {
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

    fun requireValid(item: DisputeScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeScore>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeScoreValidator(): DisputeScoreValidator {
    return DisputeScoreValidator(
        mutableListOf<DisputeScoreRule>(
            DisputeScoreTimezoneLengthRule(3600),
            DisputeScoreRequiredCodeRule("dispute score needs an entry"),
            DisputeScoreCodePrefixRule("ACM"),
            DisputeScoreQuantityRangeRule(64, 3600),
            DisputeScoreCreatedAtRangeRule(64, 3600),
        ),
    )
}
