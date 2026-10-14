package com.acme.transport.lastmile.proofofdelivery.mapping

/** A rule a proof of delivery digest must pass. Check returns its problems, or none. */
interface ProofOfDeliveryDigestRule {
    fun check(item: ProofOfDeliveryDigest): MutableList<String>
}

data class ProofOfDeliveryDigestApprovedGuardRule(var limit: Int) : ProofOfDeliveryDigestRule {
    override fun check(item: ProofOfDeliveryDigest): MutableList<String> {
        if (item.approved && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryDigestRequiredCategoryRule(var message: String) : ProofOfDeliveryDigestRule {
    override fun check(item: ProofOfDeliveryDigest): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryDigestRequiredLabelRule(var message: String) : ProofOfDeliveryDigestRule {
    override fun check(item: ProofOfDeliveryDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryDigestCategoryLengthRule(var maxLength: Int) : ProofOfDeliveryDigestRule {
    override fun check(item: ProofOfDeliveryDigest): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryDigestSegmentLengthRule(var maxLength: Int) : ProofOfDeliveryDigestRule {
    override fun check(item: ProofOfDeliveryDigest): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery digest. */
data class ProofOfDeliveryDigestValidator(var rules: MutableList<ProofOfDeliveryDigestRule>) {
    fun validate(item: ProofOfDeliveryDigest): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryDigestValidator(): ProofOfDeliveryDigestValidator {
    return ProofOfDeliveryDigestValidator(
        mutableListOf<ProofOfDeliveryDigestRule>(
            ProofOfDeliveryDigestApprovedGuardRule(86400),
            ProofOfDeliveryDigestRequiredCategoryRule("proof of delivery digest needs a value"),
            ProofOfDeliveryDigestRequiredLabelRule("proof of delivery digest needs a value"),
            ProofOfDeliveryDigestCategoryLengthRule(64),
            ProofOfDeliveryDigestSegmentLengthRule(16),
        ),
    )
}
