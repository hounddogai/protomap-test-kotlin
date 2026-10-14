package com.acme.transport.freightpay.accessorial.cycle

/** A rule a accessorial review must pass. Check returns its problems, or none. */
interface AccessorialReviewRule {
    fun check(item: AccessorialReview): MutableList<String>
}

data class AccessorialReviewMaximumRangeRule(var min: Double, var max: Double) : AccessorialReviewRule {
    override fun check(item: AccessorialReview): MutableList<String> {
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

data class AccessorialReviewTitleLengthRule(var maxLength: Int) : AccessorialReviewRule {
    override fun check(item: AccessorialReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialReviewVehicleIdLengthRule(var maxLength: Int) : AccessorialReviewRule {
    override fun check(item: AccessorialReview): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialReviewRatioRangeRule(var min: Double, var max: Double) : AccessorialReviewRule {
    override fun check(item: AccessorialReview): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class AccessorialReviewCeilingRangeRule(var min: Double, var max: Double) : AccessorialReviewRule {
    override fun check(item: AccessorialReview): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a accessorial review. */
data class AccessorialReviewValidator(var rules: MutableList<AccessorialReviewRule>) {
    fun validate(item: AccessorialReview): MutableList<String> {
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

    fun requireValid(item: AccessorialReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialReview>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialReviewValidator(): AccessorialReviewValidator {
    return AccessorialReviewValidator(
        mutableListOf<AccessorialReviewRule>(
            AccessorialReviewMaximumRangeRule(3.0, 4903.3),
            AccessorialReviewTitleLengthRule(86400),
            AccessorialReviewVehicleIdLengthRule(3600),
            AccessorialReviewRatioRangeRule(5.8, 1394.6),
            AccessorialReviewCeilingRangeRule(0.6, 2286.3),
        ),
    )
}
