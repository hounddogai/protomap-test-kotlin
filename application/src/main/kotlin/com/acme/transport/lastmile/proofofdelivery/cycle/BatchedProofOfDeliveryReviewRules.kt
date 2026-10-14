package com.acme.transport.lastmile.proofofdelivery.cycle

/** A rule a batched proof of delivery review must pass. Check returns its problems, or none. */
interface BatchedProofOfDeliveryReviewRule {
    fun check(item: BatchedProofOfDeliveryReview): MutableList<String>
}

data class BatchedProofOfDeliveryReviewCodeLengthRule(var maxLength: Int) : BatchedProofOfDeliveryReviewRule {
    override fun check(item: BatchedProofOfDeliveryReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryReviewNameLengthRule(var maxLength: Int) : BatchedProofOfDeliveryReviewRule {
    override fun check(item: BatchedProofOfDeliveryReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryReviewCodePrefixRule(var prefix: String) : BatchedProofOfDeliveryReviewRule {
    override fun check(item: BatchedProofOfDeliveryReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryReviewRequiredNameRule(
    var message: String,
) : BatchedProofOfDeliveryReviewRule {
    override fun check(item: BatchedProofOfDeliveryReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryReviewCarrierLengthRule(
    var maxLength: Int,
) : BatchedProofOfDeliveryReviewRule {
    override fun check(item: BatchedProofOfDeliveryReview): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryReviewScoreRangeRule(
    var min: Double,
    var max: Double,
) : BatchedProofOfDeliveryReviewRule {
    override fun check(item: BatchedProofOfDeliveryReview): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched proof of delivery review. */
data class BatchedProofOfDeliveryReviewValidator(var rules: MutableList<BatchedProofOfDeliveryReviewRule>) {
    fun validate(item: BatchedProofOfDeliveryReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedProofOfDeliveryReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedProofOfDeliveryReview>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedProofOfDeliveryReviewValidator(): BatchedProofOfDeliveryReviewValidator {
    return BatchedProofOfDeliveryReviewValidator(
        mutableListOf<BatchedProofOfDeliveryReviewRule>(
            BatchedProofOfDeliveryReviewCodeLengthRule(120),
            BatchedProofOfDeliveryReviewNameLengthRule(255),
            BatchedProofOfDeliveryReviewCodePrefixRule("X"),
            BatchedProofOfDeliveryReviewRequiredNameRule("batched proof of delivery review needs an entry"),
            BatchedProofOfDeliveryReviewCarrierLengthRule(255),
            BatchedProofOfDeliveryReviewScoreRangeRule(0.0, 126.1),
        ),
    )
}
