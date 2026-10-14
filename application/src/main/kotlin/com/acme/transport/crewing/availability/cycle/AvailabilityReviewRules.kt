package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.text.normalize

/** A rule a availability review must pass. Check returns its problems, or none. */
interface AvailabilityReviewRule {
    fun check(item: AvailabilityReview): MutableList<String>
}

data class AvailabilityReviewRequiredVehicleIdRule(var message: String) : AvailabilityReviewRule {
    override fun check(item: AvailabilityReview): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityReviewSequenceRangeRule(var min: Int, var max: Int) : AvailabilityReviewRule {
    override fun check(item: AvailabilityReview): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityReviewAllowedNameRule(var allowed: MutableList<String>) : AvailabilityReviewRule {
    override fun check(item: AvailabilityReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityReviewNameLengthRule(var maxLength: Int) : AvailabilityReviewRule {
    override fun check(item: AvailabilityReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityReviewRequiredCodeRule(var message: String) : AvailabilityReviewRule {
    override fun check(item: AvailabilityReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityReviewAmountRangeRule(var min: Double, var max: Double) : AvailabilityReviewRule {
    override fun check(item: AvailabilityReview): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a availability review. */
data class AvailabilityReviewValidator(var rules: MutableList<AvailabilityReviewRule>) {
    fun validate(item: AvailabilityReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityReview>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityReviewValidator(): AvailabilityReviewValidator {
    return AvailabilityReviewValidator(
        mutableListOf<AvailabilityReviewRule>(
            AvailabilityReviewRequiredVehicleIdRule("availability review needs an entry"),
            AvailabilityReviewSequenceRangeRule(255, 86400),
            AvailabilityReviewAllowedNameRule(mutableListOf<String>("capped", "tiered")),
            AvailabilityReviewNameLengthRule(120),
            AvailabilityReviewRequiredCodeRule("availability review needs this field"),
            AvailabilityReviewAmountRangeRule(6.1, 1951.4),
        ),
    )
}
