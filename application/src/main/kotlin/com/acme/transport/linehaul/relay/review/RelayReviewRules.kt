package com.acme.transport.linehaul.relay.review

/** A rule a relay review must pass. Check returns its problems, or none. */
interface RelayReviewRule {
    fun check(item: RelayReview): MutableList<String>
}

data class RelayReviewRegionLengthRule(var maxLength: Int) : RelayReviewRule {
    override fun check(item: RelayReview): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayReviewCodeLengthRule(var maxLength: Int) : RelayReviewRule {
    override fun check(item: RelayReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayReviewCodePrefixRule(var prefix: String) : RelayReviewRule {
    override fun check(item: RelayReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RelayReviewRetriesRangeRule(var min: Int, var max: Int) : RelayReviewRule {
    override fun check(item: RelayReview): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayReviewNameLengthRule(var maxLength: Int) : RelayReviewRule {
    override fun check(item: RelayReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayReviewRequiredVehicleIdRule(var message: String) : RelayReviewRule {
    override fun check(item: RelayReview): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay review. */
data class RelayReviewValidator(var rules: MutableList<RelayReviewRule>) {
    fun validate(item: RelayReview): MutableList<String> {
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

    fun requireValid(item: RelayReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayReviewValidator(): RelayReviewValidator {
    return RelayReviewValidator(
        mutableListOf<RelayReviewRule>(
            RelayReviewRegionLengthRule(32),
            RelayReviewCodeLengthRule(32),
            RelayReviewCodePrefixRule("Q"),
            RelayReviewRetriesRangeRule(120, 1000),
            RelayReviewNameLengthRule(64),
            RelayReviewRequiredVehicleIdRule("relay review needs this field"),
        ),
    )
}
