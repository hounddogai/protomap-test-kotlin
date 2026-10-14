package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.text.normalize

/** A rule a parcel quota must pass. Check returns its problems, or none. */
interface ParcelQuotaRule {
    fun check(item: ParcelQuota): MutableList<String>
}

data class ParcelQuotaReviewerLengthRule(var maxLength: Int) : ParcelQuotaRule {
    override fun check(item: ParcelQuota): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelQuotaAllowedContactEmailRule(var allowed: MutableList<String>) : ParcelQuotaRule {
    override fun check(item: ParcelQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ParcelQuotaRequiredReviewerRule(var message: String) : ParcelQuotaRule {
    override fun check(item: ParcelQuota): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelQuotaContactEmailLengthRule(var maxLength: Int) : ParcelQuotaRule {
    override fun check(item: ParcelQuota): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelQuotaOdometerRangeRule(var min: Int, var max: Int) : ParcelQuotaRule {
    override fun check(item: ParcelQuota): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class ParcelQuotaRequiredTitleRule(var message: String) : ParcelQuotaRule {
    override fun check(item: ParcelQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel quota. */
data class ParcelQuotaValidator(var rules: MutableList<ParcelQuotaRule>) {
    fun validate(item: ParcelQuota): MutableList<String> {
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

    fun requireValid(item: ParcelQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelQuotaValidator(): ParcelQuotaValidator {
    return ParcelQuotaValidator(
        mutableListOf<ParcelQuotaRule>(
            ParcelQuotaReviewerLengthRule(86400),
            ParcelQuotaAllowedContactEmailRule(mutableListOf<String>("regional", "manual")),
            ParcelQuotaRequiredReviewerRule("parcel quota needs an entry"),
            ParcelQuotaContactEmailLengthRule(86400),
            ParcelQuotaOdometerRangeRule(1000, 86400),
            ParcelQuotaRequiredTitleRule("parcel quota needs this field"),
        ),
    )
}
