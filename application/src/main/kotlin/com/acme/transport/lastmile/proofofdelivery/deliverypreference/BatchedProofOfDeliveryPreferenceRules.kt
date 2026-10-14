package com.acme.transport.lastmile.proofofdelivery.deliverypreference

/** A rule a batched proof of delivery preference must pass. Check returns its problems, or none. */
interface BatchedProofOfDeliveryPreferenceRule {
    fun check(item: BatchedProofOfDeliveryPreference): MutableList<String>
}

data class BatchedProofOfDeliveryPreferenceRequiredCodeRule(
    var message: String,
) : BatchedProofOfDeliveryPreferenceRule {
    override fun check(item: BatchedProofOfDeliveryPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryPreferenceLabelLengthRule(
    var maxLength: Int,
) : BatchedProofOfDeliveryPreferenceRule {
    override fun check(item: BatchedProofOfDeliveryPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryPreferenceActiveGuardRule(
    var limit: Int,
) : BatchedProofOfDeliveryPreferenceRule {
    override fun check(item: BatchedProofOfDeliveryPreference): MutableList<String> {
        if (item.active && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryPreferenceFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : BatchedProofOfDeliveryPreferenceRule {
    override fun check(item: BatchedProofOfDeliveryPreference): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched proof of delivery preference. */
data class BatchedProofOfDeliveryPreferenceValidator(
    var rules: MutableList<BatchedProofOfDeliveryPreferenceRule>,
) {
    fun validate(item: BatchedProofOfDeliveryPreference): MutableList<String> {
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

    fun requireValid(item: BatchedProofOfDeliveryPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedProofOfDeliveryPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedProofOfDeliveryPreferenceValidator(): BatchedProofOfDeliveryPreferenceValidator {
    return BatchedProofOfDeliveryPreferenceValidator(
        mutableListOf<BatchedProofOfDeliveryPreferenceRule>(
            BatchedProofOfDeliveryPreferenceRequiredCodeRule(
                "batched proof of delivery preference needs an entry",
            ),
            BatchedProofOfDeliveryPreferenceLabelLengthRule(1000),
            BatchedProofOfDeliveryPreferenceActiveGuardRule(32),
            BatchedProofOfDeliveryPreferenceFuelLitersRangeRule(7.1, 3236.0),
        ),
    )
}
