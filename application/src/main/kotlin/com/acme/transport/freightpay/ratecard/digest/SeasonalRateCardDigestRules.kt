package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.kit.text.normalize

/** A rule a seasonal rate card digest must pass. Check returns its problems, or none. */
interface SeasonalRateCardDigestRule {
    fun check(item: SeasonalRateCardDigest): MutableList<String>
}

data class SeasonalRateCardDigestLabelLengthRule(var maxLength: Int) : SeasonalRateCardDigestRule {
    override fun check(item: SeasonalRateCardDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRateCardDigestCodeLengthRule(var maxLength: Int) : SeasonalRateCardDigestRule {
    override fun check(item: SeasonalRateCardDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRateCardDigestLocaleLengthRule(var maxLength: Int) : SeasonalRateCardDigestRule {
    override fun check(item: SeasonalRateCardDigest): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRateCardDigestRequiredLocaleRule(var message: String) : SeasonalRateCardDigestRule {
    override fun check(item: SeasonalRateCardDigest): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRateCardDigestAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalRateCardDigestRule {
    override fun check(item: SeasonalRateCardDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal rate card digest. */
data class SeasonalRateCardDigestValidator(var rules: MutableList<SeasonalRateCardDigestRule>) {
    fun validate(item: SeasonalRateCardDigest): MutableList<String> {
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

    fun requireValid(item: SeasonalRateCardDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRateCardDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRateCardDigestValidator(): SeasonalRateCardDigestValidator {
    return SeasonalRateCardDigestValidator(
        mutableListOf<SeasonalRateCardDigestRule>(
            SeasonalRateCardDigestLabelLengthRule(80),
            SeasonalRateCardDigestCodeLengthRule(3600),
            SeasonalRateCardDigestLocaleLengthRule(64),
            SeasonalRateCardDigestRequiredLocaleRule("seasonal rate card digest needs an entry"),
            SeasonalRateCardDigestAllowedCodeRule(mutableListOf<String>("tiered", "batched")),
        ),
    )
}
