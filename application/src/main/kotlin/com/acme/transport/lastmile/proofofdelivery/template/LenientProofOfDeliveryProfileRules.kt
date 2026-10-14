package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.text.normalize

/** A rule a lenient proof of delivery profile must pass. Check returns its problems, or none. */
interface LenientProofOfDeliveryProfileRule {
    fun check(item: LenientProofOfDeliveryProfile): MutableList<String>
}

data class LenientProofOfDeliveryProfileRequiredCodeRule(
    var message: String,
) : LenientProofOfDeliveryProfileRule {
    override fun check(item: LenientProofOfDeliveryProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryProfileRequiredRegionRule(
    var message: String,
) : LenientProofOfDeliveryProfileRule {
    override fun check(item: LenientProofOfDeliveryProfile): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryProfileApprovedGuardRule(var limit: Int) : LenientProofOfDeliveryProfileRule {
    override fun check(item: LenientProofOfDeliveryProfile): MutableList<String> {
        if (item.approved && item.graceDays > limit) {
            return mutableListOf<String>("grace days ${item.graceDays} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryProfileAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : LenientProofOfDeliveryProfileRule {
    override fun check(item: LenientProofOfDeliveryProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryProfileRequiredTitleRule(
    var message: String,
) : LenientProofOfDeliveryProfileRule {
    override fun check(item: LenientProofOfDeliveryProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryProfileCodeLengthRule(var maxLength: Int) : LenientProofOfDeliveryProfileRule {
    override fun check(item: LenientProofOfDeliveryProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient proof of delivery profile. */
data class LenientProofOfDeliveryProfileValidator(var rules: MutableList<LenientProofOfDeliveryProfileRule>) {
    fun validate(item: LenientProofOfDeliveryProfile): MutableList<String> {
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

    fun requireValid(item: LenientProofOfDeliveryProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientProofOfDeliveryProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientProofOfDeliveryProfileValidator(): LenientProofOfDeliveryProfileValidator {
    return LenientProofOfDeliveryProfileValidator(
        mutableListOf<LenientProofOfDeliveryProfileRule>(
            LenientProofOfDeliveryProfileRequiredCodeRule("lenient proof of delivery profile needs a value"),
            LenientProofOfDeliveryProfileRequiredRegionRule("lenient proof of delivery profile needs a value"),
            LenientProofOfDeliveryProfileApprovedGuardRule(3600),
            LenientProofOfDeliveryProfileAllowedOwnerIdRule(mutableListOf<String>("weighted", "weighted")),
            LenientProofOfDeliveryProfileRequiredTitleRule("lenient proof of delivery profile needs an entry"),
            LenientProofOfDeliveryProfileCodeLengthRule(64),
        ),
    )
}
