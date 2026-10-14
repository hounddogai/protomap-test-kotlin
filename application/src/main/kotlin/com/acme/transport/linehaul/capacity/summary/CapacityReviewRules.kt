package com.acme.transport.linehaul.capacity.summary

/** A rule a capacity review must pass. Check returns its problems, or none. */
interface CapacityReviewRule {
    fun check(item: CapacityReview): MutableList<String>
}

data class CapacityReviewCodePrefixRule(var prefix: String) : CapacityReviewRule {
    override fun check(item: CapacityReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CapacityReviewMarginRangeRule(var min: Double, var max: Double) : CapacityReviewRule {
    override fun check(item: CapacityReview): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class CapacityReviewGraceDaysRangeRule(var min: Int, var max: Int) : CapacityReviewRule {
    override fun check(item: CapacityReview): MutableList<String> {
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

data class CapacityReviewCountryLengthRule(var maxLength: Int) : CapacityReviewRule {
    override fun check(item: CapacityReview): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityReviewRequiredCodeRule(var message: String) : CapacityReviewRule {
    override fun check(item: CapacityReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity review. */
data class CapacityReviewValidator(var rules: MutableList<CapacityReviewRule>) {
    fun validate(item: CapacityReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityReview>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityReviewValidator(): CapacityReviewValidator {
    return CapacityReviewValidator(
        mutableListOf<CapacityReviewRule>(
            CapacityReviewCodePrefixRule("ACM"),
            CapacityReviewMarginRangeRule(3.8, 2287.2),
            CapacityReviewGraceDaysRangeRule(1000, 86400),
            CapacityReviewCountryLengthRule(86400),
            CapacityReviewRequiredCodeRule("capacity review needs a value"),
        ),
    )
}
