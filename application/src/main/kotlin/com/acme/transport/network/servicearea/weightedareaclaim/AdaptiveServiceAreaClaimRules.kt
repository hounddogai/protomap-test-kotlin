package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.kit.text.normalize

/** A rule a adaptive service area claim must pass. Check returns its problems, or none. */
interface AdaptiveServiceAreaClaimRule {
    fun check(item: AdaptiveServiceAreaClaim): MutableList<String>
}

data class AdaptiveServiceAreaClaimReviewerLengthRule(var maxLength: Int) : AdaptiveServiceAreaClaimRule {
    override fun check(item: AdaptiveServiceAreaClaim): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServiceAreaClaimRequiredTitleRule(var message: String) : AdaptiveServiceAreaClaimRule {
    override fun check(item: AdaptiveServiceAreaClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServiceAreaClaimCodeLengthRule(var maxLength: Int) : AdaptiveServiceAreaClaimRule {
    override fun check(item: AdaptiveServiceAreaClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServiceAreaClaimAllowedReviewerRule(
    var allowed: MutableList<String>,
) : AdaptiveServiceAreaClaimRule {
    override fun check(item: AdaptiveServiceAreaClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServiceAreaClaimPhoneLengthRule(var maxLength: Int) : AdaptiveServiceAreaClaimRule {
    override fun check(item: AdaptiveServiceAreaClaim): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive service area claim. */
data class AdaptiveServiceAreaClaimValidator(var rules: MutableList<AdaptiveServiceAreaClaimRule>) {
    fun validate(item: AdaptiveServiceAreaClaim): MutableList<String> {
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

    fun requireValid(item: AdaptiveServiceAreaClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveServiceAreaClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveServiceAreaClaimValidator(): AdaptiveServiceAreaClaimValidator {
    return AdaptiveServiceAreaClaimValidator(
        mutableListOf<AdaptiveServiceAreaClaimRule>(
            AdaptiveServiceAreaClaimReviewerLengthRule(1000),
            AdaptiveServiceAreaClaimRequiredTitleRule("adaptive service area claim needs this field"),
            AdaptiveServiceAreaClaimCodeLengthRule(86400),
            AdaptiveServiceAreaClaimAllowedReviewerRule(mutableListOf<String>()),
            AdaptiveServiceAreaClaimPhoneLengthRule(64),
        ),
    )
}
