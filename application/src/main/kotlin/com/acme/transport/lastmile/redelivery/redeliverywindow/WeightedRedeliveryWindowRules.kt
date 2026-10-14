package com.acme.transport.lastmile.redelivery.redeliverywindow

/** A rule a weighted redelivery window must pass. Check returns its problems, or none. */
interface WeightedRedeliveryWindowRule {
    fun check(item: WeightedRedeliveryWindow): MutableList<String>
}

data class WeightedRedeliveryWindowRetriesRangeRule(var min: Int, var max: Int) : WeightedRedeliveryWindowRule {
    override fun check(item: WeightedRedeliveryWindow): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRedeliveryWindowPriorityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedRedeliveryWindowRule {
    override fun check(item: WeightedRedeliveryWindow): MutableList<String> {
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

data class WeightedRedeliveryWindowCategoryLengthRule(var maxLength: Int) : WeightedRedeliveryWindowRule {
    override fun check(item: WeightedRedeliveryWindow): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRedeliveryWindowRequiredCategoryRule(var message: String) : WeightedRedeliveryWindowRule {
    override fun check(item: WeightedRedeliveryWindow): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRedeliveryWindowCodePrefixRule(var prefix: String) : WeightedRedeliveryWindowRule {
    override fun check(item: WeightedRedeliveryWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted redelivery window. */
data class WeightedRedeliveryWindowValidator(var rules: MutableList<WeightedRedeliveryWindowRule>) {
    fun validate(item: WeightedRedeliveryWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedRedeliveryWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRedeliveryWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRedeliveryWindowValidator(): WeightedRedeliveryWindowValidator {
    return WeightedRedeliveryWindowValidator(
        mutableListOf<WeightedRedeliveryWindowRule>(
            WeightedRedeliveryWindowRetriesRangeRule(80, 255),
            WeightedRedeliveryWindowPriorityRangeRule(64, 86400),
            WeightedRedeliveryWindowCategoryLengthRule(32),
            WeightedRedeliveryWindowRequiredCategoryRule("weighted redelivery window needs this field"),
            WeightedRedeliveryWindowCodePrefixRule("ACM"),
        ),
    )
}
