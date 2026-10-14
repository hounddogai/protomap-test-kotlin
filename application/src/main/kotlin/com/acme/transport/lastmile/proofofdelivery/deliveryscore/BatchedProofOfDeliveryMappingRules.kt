package com.acme.transport.lastmile.proofofdelivery.deliveryscore

/** A rule a batched proof of delivery mapping must pass. Check returns its problems, or none. */
interface BatchedProofOfDeliveryMappingRule {
    fun check(item: BatchedProofOfDeliveryMapping): MutableList<String>
}

data class BatchedProofOfDeliveryMappingCodePrefixRule(var prefix: String) : BatchedProofOfDeliveryMappingRule {
    override fun check(item: BatchedProofOfDeliveryMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryMappingNameLengthRule(var maxLength: Int) : BatchedProofOfDeliveryMappingRule {
    override fun check(item: BatchedProofOfDeliveryMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryMappingRevisionRangeRule(
    var min: Int,
    var max: Int,
) : BatchedProofOfDeliveryMappingRule {
    override fun check(item: BatchedProofOfDeliveryMapping): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class BatchedProofOfDeliveryMappingRequiredCodeRule(
    var message: String,
) : BatchedProofOfDeliveryMappingRule {
    override fun check(item: BatchedProofOfDeliveryMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched proof of delivery mapping. */
data class BatchedProofOfDeliveryMappingValidator(var rules: MutableList<BatchedProofOfDeliveryMappingRule>) {
    fun validate(item: BatchedProofOfDeliveryMapping): MutableList<String> {
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

    fun requireValid(item: BatchedProofOfDeliveryMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedProofOfDeliveryMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedProofOfDeliveryMappingValidator(): BatchedProofOfDeliveryMappingValidator {
    return BatchedProofOfDeliveryMappingValidator(
        mutableListOf<BatchedProofOfDeliveryMappingRule>(
            BatchedProofOfDeliveryMappingCodePrefixRule("X"),
            BatchedProofOfDeliveryMappingNameLengthRule(80),
            BatchedProofOfDeliveryMappingRevisionRangeRule(16, 120),
            BatchedProofOfDeliveryMappingRequiredCodeRule("batched proof of delivery mapping needs an entry"),
        ),
    )
}
