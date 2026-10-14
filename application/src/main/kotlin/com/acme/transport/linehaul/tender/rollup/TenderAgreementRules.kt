package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.text.normalize

/** A rule a tender agreement must pass. Check returns its problems, or none. */
interface TenderAgreementRule {
    fun check(item: TenderAgreement): MutableList<String>
}

data class TenderAgreementCarrierLengthRule(var maxLength: Int) : TenderAgreementRule {
    override fun check(item: TenderAgreement): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderAgreementCodePrefixRule(var prefix: String) : TenderAgreementRule {
    override fun check(item: TenderAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderAgreementAllowedCarrierRule(var allowed: MutableList<String>) : TenderAgreementRule {
    override fun check(item: TenderAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TenderAgreementUpdatedAtRangeRule(var min: Int, var max: Int) : TenderAgreementRule {
    override fun check(item: TenderAgreement): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderAgreementTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : TenderAgreementRule {
    override fun check(item: TenderAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a tender agreement. */
data class TenderAgreementValidator(var rules: MutableList<TenderAgreementRule>) {
    fun validate(item: TenderAgreement): MutableList<String> {
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

    fun requireValid(item: TenderAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderAgreementValidator(): TenderAgreementValidator {
    return TenderAgreementValidator(
        mutableListOf<TenderAgreementRule>(
            TenderAgreementCarrierLengthRule(80),
            TenderAgreementCodePrefixRule("X"),
            TenderAgreementAllowedCarrierRule(mutableListOf<String>("tiered", "tiered")),
            TenderAgreementUpdatedAtRangeRule(120, 1000),
            TenderAgreementTagsLimitRule(80, mutableListOf<String>("legacy")),
        ),
    )
}
