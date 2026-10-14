package com.acme.transport.lastmile.proofofdelivery.deliveryreview

/** A rule a regional proof of delivery review must pass. Check returns its problems, or none. */
interface RegionalProofOfDeliveryReviewRule {
    fun check(item: RegionalProofOfDeliveryReview): MutableList<String>
}

data class RegionalProofOfDeliveryReviewNameLengthRule(var maxLength: Int) : RegionalProofOfDeliveryReviewRule {
    override fun check(item: RegionalProofOfDeliveryReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalProofOfDeliveryReviewDockLengthRule(var maxLength: Int) : RegionalProofOfDeliveryReviewRule {
    override fun check(item: RegionalProofOfDeliveryReview): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalProofOfDeliveryReviewBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : RegionalProofOfDeliveryReviewRule {
    override fun check(item: RegionalProofOfDeliveryReview): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class RegionalProofOfDeliveryReviewContactEmailLengthRule(
    var maxLength: Int,
) : RegionalProofOfDeliveryReviewRule {
    override fun check(item: RegionalProofOfDeliveryReview): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalProofOfDeliveryReviewRequiredCodeRule(
    var message: String,
) : RegionalProofOfDeliveryReviewRule {
    override fun check(item: RegionalProofOfDeliveryReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalProofOfDeliveryReviewRequiredNameRule(
    var message: String,
) : RegionalProofOfDeliveryReviewRule {
    override fun check(item: RegionalProofOfDeliveryReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional proof of delivery review. */
data class RegionalProofOfDeliveryReviewValidator(var rules: MutableList<RegionalProofOfDeliveryReviewRule>) {
    fun validate(item: RegionalProofOfDeliveryReview): MutableList<String> {
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

    fun requireValid(item: RegionalProofOfDeliveryReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalProofOfDeliveryReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalProofOfDeliveryReviewValidator(): RegionalProofOfDeliveryReviewValidator {
    return RegionalProofOfDeliveryReviewValidator(
        mutableListOf<RegionalProofOfDeliveryReviewRule>(
            RegionalProofOfDeliveryReviewNameLengthRule(3600),
            RegionalProofOfDeliveryReviewDockLengthRule(86400),
            RegionalProofOfDeliveryReviewBatchSizeRangeRule(32, 120),
            RegionalProofOfDeliveryReviewContactEmailLengthRule(32),
            RegionalProofOfDeliveryReviewRequiredCodeRule("regional proof of delivery review needs an entry"),
            RegionalProofOfDeliveryReviewRequiredNameRule("regional proof of delivery review needs a value"),
        ),
    )
}
