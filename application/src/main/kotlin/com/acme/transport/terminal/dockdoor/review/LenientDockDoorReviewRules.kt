package com.acme.transport.terminal.dockdoor.review

/** A rule a lenient dock door review must pass. Check returns its problems, or none. */
interface LenientDockDoorReviewRule {
    fun check(item: LenientDockDoorReview): MutableList<String>
}

data class LenientDockDoorReviewRequiredNameRule(var message: String) : LenientDockDoorReviewRule {
    override fun check(item: LenientDockDoorReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDockDoorReviewCodePrefixRule(var prefix: String) : LenientDockDoorReviewRule {
    override fun check(item: LenientDockDoorReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientDockDoorReviewApprovedGuardRule(var limit: Int) : LenientDockDoorReviewRule {
    override fun check(item: LenientDockDoorReview): MutableList<String> {
        if (item.approved && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class LenientDockDoorReviewLimitRangeRule(var min: Int, var max: Int) : LenientDockDoorReviewRule {
    override fun check(item: LenientDockDoorReview): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LenientDockDoorReviewVehicleIdLengthRule(var maxLength: Int) : LenientDockDoorReviewRule {
    override fun check(item: LenientDockDoorReview): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dock door review. */
data class LenientDockDoorReviewValidator(var rules: MutableList<LenientDockDoorReviewRule>) {
    fun validate(item: LenientDockDoorReview): MutableList<String> {
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

    fun requireValid(item: LenientDockDoorReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDockDoorReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDockDoorReviewValidator(): LenientDockDoorReviewValidator {
    return LenientDockDoorReviewValidator(
        mutableListOf<LenientDockDoorReviewRule>(
            LenientDockDoorReviewRequiredNameRule("lenient dock door review needs a value"),
            LenientDockDoorReviewCodePrefixRule("OPS"),
            LenientDockDoorReviewApprovedGuardRule(120),
            LenientDockDoorReviewLimitRangeRule(64, 1000),
            LenientDockDoorReviewVehicleIdLengthRule(16),
        ),
    )
}
