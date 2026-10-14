package com.acme.transport.freightpay.accessorial.accessorialmetric

/** A rule a scheduled accessorial review must pass. Check returns its problems, or none. */
interface ScheduledAccessorialReviewRule {
    fun check(item: ScheduledAccessorialReview): MutableList<String>
}

data class ScheduledAccessorialReviewRequiredLabelRule(var message: String) : ScheduledAccessorialReviewRule {
    override fun check(item: ScheduledAccessorialReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialReviewCapacityRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledAccessorialReviewRule {
    override fun check(item: ScheduledAccessorialReview): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialReviewCodePrefixRule(var prefix: String) : ScheduledAccessorialReviewRule {
    override fun check(item: ScheduledAccessorialReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialReviewFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledAccessorialReviewRule {
    override fun check(item: ScheduledAccessorialReview): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ScheduledAccessorialReviewMarginRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledAccessorialReviewRule {
    override fun check(item: ScheduledAccessorialReview): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAccessorialReviewRequiredCodeRule(var message: String) : ScheduledAccessorialReviewRule {
    override fun check(item: ScheduledAccessorialReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled accessorial review. */
data class ScheduledAccessorialReviewValidator(var rules: MutableList<ScheduledAccessorialReviewRule>) {
    fun validate(item: ScheduledAccessorialReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAccessorialReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAccessorialReview>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAccessorialReviewValidator(): ScheduledAccessorialReviewValidator {
    return ScheduledAccessorialReviewValidator(
        mutableListOf<ScheduledAccessorialReviewRule>(
            ScheduledAccessorialReviewRequiredLabelRule("scheduled accessorial review needs this field"),
            ScheduledAccessorialReviewCapacityRangeRule(120, 3600),
            ScheduledAccessorialReviewCodePrefixRule("REF"),
            ScheduledAccessorialReviewFuelLitersRangeRule(9.6, 305.1),
            ScheduledAccessorialReviewMarginRangeRule(4.9, 3202.0),
            ScheduledAccessorialReviewRequiredCodeRule("scheduled accessorial review needs an entry"),
        ),
    )
}
