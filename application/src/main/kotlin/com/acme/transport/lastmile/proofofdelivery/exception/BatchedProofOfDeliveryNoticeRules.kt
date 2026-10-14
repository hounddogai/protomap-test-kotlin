package com.acme.transport.lastmile.proofofdelivery.exception

/** A rule a batched proof of delivery notice must pass. Check returns its problems, or none. */
interface BatchedProofOfDeliveryNoticeRule {
    fun check(item: BatchedProofOfDeliveryNotice): MutableList<String>
}

data class BatchedProofOfDeliveryNoticeCodeLengthRule(var maxLength: Int) : BatchedProofOfDeliveryNoticeRule {
    override fun check(item: BatchedProofOfDeliveryNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryNoticeVisibleGuardRule(var limit: Int) : BatchedProofOfDeliveryNoticeRule {
    override fun check(item: BatchedProofOfDeliveryNotice): MutableList<String> {
        if (item.visible && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryNoticeRequiredExternalRefRule(
    var message: String,
) : BatchedProofOfDeliveryNoticeRule {
    override fun check(item: BatchedProofOfDeliveryNotice): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryNoticeVersionRangeRule(
    var min: Int,
    var max: Int,
) : BatchedProofOfDeliveryNoticeRule {
    override fun check(item: BatchedProofOfDeliveryNotice): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched proof of delivery notice. */
data class BatchedProofOfDeliveryNoticeValidator(var rules: MutableList<BatchedProofOfDeliveryNoticeRule>) {
    fun validate(item: BatchedProofOfDeliveryNotice): MutableList<String> {
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

    fun requireValid(item: BatchedProofOfDeliveryNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedProofOfDeliveryNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedProofOfDeliveryNoticeValidator(): BatchedProofOfDeliveryNoticeValidator {
    return BatchedProofOfDeliveryNoticeValidator(
        mutableListOf<BatchedProofOfDeliveryNoticeRule>(
            BatchedProofOfDeliveryNoticeCodeLengthRule(80),
            BatchedProofOfDeliveryNoticeVisibleGuardRule(1000),
            BatchedProofOfDeliveryNoticeRequiredExternalRefRule(
                "batched proof of delivery notice needs this field",
            ),
            BatchedProofOfDeliveryNoticeVersionRangeRule(120, 3600),
        ),
    )
}
