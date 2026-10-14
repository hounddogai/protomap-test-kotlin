package com.acme.transport.terminal.dwell.review

/** A rule a default dwell review must pass. Check returns its problems, or none. */
interface DefaultDwellReviewRule {
    fun check(item: DefaultDwellReview): MutableList<String>
}

data class DefaultDwellReviewVersionRangeRule(var min: Int, var max: Int) : DefaultDwellReviewRule {
    override fun check(item: DefaultDwellReview): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class DefaultDwellReviewCodeLengthRule(var maxLength: Int) : DefaultDwellReviewRule {
    override fun check(item: DefaultDwellReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellReviewRequiredCodeRule(var message: String) : DefaultDwellReviewRule {
    override fun check(item: DefaultDwellReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellReviewChannelLengthRule(var maxLength: Int) : DefaultDwellReviewRule {
    override fun check(item: DefaultDwellReview): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellReviewCodePrefixRule(var prefix: String) : DefaultDwellReviewRule {
    override fun check(item: DefaultDwellReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellReviewPriorityRangeRule(var min: Int, var max: Int) : DefaultDwellReviewRule {
    override fun check(item: DefaultDwellReview): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default dwell review. */
data class DefaultDwellReviewValidator(var rules: MutableList<DefaultDwellReviewRule>) {
    fun validate(item: DefaultDwellReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDwellReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDwellReview>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDwellReviewValidator(): DefaultDwellReviewValidator {
    return DefaultDwellReviewValidator(
        mutableListOf<DefaultDwellReviewRule>(
            DefaultDwellReviewVersionRangeRule(64, 80),
            DefaultDwellReviewCodeLengthRule(1000),
            DefaultDwellReviewRequiredCodeRule("default dwell review needs a value"),
            DefaultDwellReviewChannelLengthRule(255),
            DefaultDwellReviewCodePrefixRule("REF"),
            DefaultDwellReviewPriorityRangeRule(120, 255),
        ),
    )
}
