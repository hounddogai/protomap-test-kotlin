package com.acme.transport.terminal.sortation.review

/** A rule a sortation review must pass. Check returns its problems, or none. */
interface SortationReviewRule {
    fun check(item: SortationReview): MutableList<String>
}

data class SortationReviewRequiredCodeRule(var message: String) : SortationReviewRule {
    override fun check(item: SortationReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationReviewNameLengthRule(var maxLength: Int) : SortationReviewRule {
    override fun check(item: SortationReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationReviewCodePrefixRule(var prefix: String) : SortationReviewRule {
    override fun check(item: SortationReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationReviewRequiredRegionRule(var message: String) : SortationReviewRule {
    override fun check(item: SortationReview): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationReviewDriverIdLengthRule(var maxLength: Int) : SortationReviewRule {
    override fun check(item: SortationReview): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationReviewCostRangeRule(var min: Double, var max: Double) : SortationReviewRule {
    override fun check(item: SortationReview): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a sortation review. */
data class SortationReviewValidator(var rules: MutableList<SortationReviewRule>) {
    fun validate(item: SortationReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationReviewValidator(): SortationReviewValidator {
    return SortationReviewValidator(
        mutableListOf<SortationReviewRule>(
            SortationReviewRequiredCodeRule("sortation review needs this field"),
            SortationReviewNameLengthRule(1000),
            SortationReviewCodePrefixRule("OPS"),
            SortationReviewRequiredRegionRule("sortation review needs a value"),
            SortationReviewDriverIdLengthRule(86400),
            SortationReviewCostRangeRule(9.1, 74.9),
        ),
    )
}
