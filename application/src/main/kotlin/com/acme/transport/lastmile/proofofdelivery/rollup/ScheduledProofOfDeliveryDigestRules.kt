package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.text.normalize

/** A rule a scheduled proof of delivery digest must pass. Check returns its problems, or none. */
interface ScheduledProofOfDeliveryDigestRule {
    fun check(item: ScheduledProofOfDeliveryDigest): MutableList<String>
}

data class ScheduledProofOfDeliveryDigestAllowedCategoryRule(
    var allowed: MutableList<String>,
) : ScheduledProofOfDeliveryDigestRule {
    override fun check(item: ScheduledProofOfDeliveryDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryDigestCategoryLengthRule(
    var maxLength: Int,
) : ScheduledProofOfDeliveryDigestRule {
    override fun check(item: ScheduledProofOfDeliveryDigest): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryDigestCodePrefixRule(
    var prefix: String,
) : ScheduledProofOfDeliveryDigestRule {
    override fun check(item: ScheduledProofOfDeliveryDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledProofOfDeliveryDigestRequiredCategoryRule(
    var message: String,
) : ScheduledProofOfDeliveryDigestRule {
    override fun check(item: ScheduledProofOfDeliveryDigest): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled proof of delivery digest. */
data class ScheduledProofOfDeliveryDigestValidator(var rules: MutableList<ScheduledProofOfDeliveryDigestRule>) {
    fun validate(item: ScheduledProofOfDeliveryDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledProofOfDeliveryDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledProofOfDeliveryDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledProofOfDeliveryDigestValidator(): ScheduledProofOfDeliveryDigestValidator {
    return ScheduledProofOfDeliveryDigestValidator(
        mutableListOf<ScheduledProofOfDeliveryDigestRule>(
            ScheduledProofOfDeliveryDigestAllowedCategoryRule(mutableListOf<String>()),
            ScheduledProofOfDeliveryDigestCategoryLengthRule(64),
            ScheduledProofOfDeliveryDigestCodePrefixRule("OPS"),
            ScheduledProofOfDeliveryDigestRequiredCategoryRule(
                "scheduled proof of delivery digest needs a value",
            ),
        ),
    )
}
