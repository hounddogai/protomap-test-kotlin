package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.text.normalize

/** A rule a tender tier must pass. Check returns its problems, or none. */
interface TenderTierRule {
    fun check(item: TenderTier): MutableList<String>
}

data class TenderTierRequiredPhoneRule(var message: String) : TenderTierRule {
    override fun check(item: TenderTier): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderTierVisibleGuardRule(var limit: Double) : TenderTierRule {
    override fun check(item: TenderTier): MutableList<String> {
        if (item.visible && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class TenderTierAllowedPhoneRule(var allowed: MutableList<String>) : TenderTierRule {
    override fun check(item: TenderTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TenderTierPhoneLengthRule(var maxLength: Int) : TenderTierRule {
    override fun check(item: TenderTier): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderTierNameLengthRule(var maxLength: Int) : TenderTierRule {
    override fun check(item: TenderTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender tier. */
data class TenderTierValidator(var rules: MutableList<TenderTierRule>) {
    fun validate(item: TenderTier): MutableList<String> {
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

    fun requireValid(item: TenderTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderTier>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderTierValidator(): TenderTierValidator {
    return TenderTierValidator(
        mutableListOf<TenderTierRule>(
            TenderTierRequiredPhoneRule("tender tier needs an entry"),
            TenderTierVisibleGuardRule(0.8),
            TenderTierAllowedPhoneRule(mutableListOf<String>("default", "capped", "scheduled")),
            TenderTierPhoneLengthRule(64),
            TenderTierNameLengthRule(32),
        ),
    )
}
