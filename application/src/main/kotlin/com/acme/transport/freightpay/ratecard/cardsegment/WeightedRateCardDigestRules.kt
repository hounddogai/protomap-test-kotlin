package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.text.normalize

/** A rule a weighted rate card digest must pass. Check returns its problems, or none. */
interface WeightedRateCardDigestRule {
    fun check(item: WeightedRateCardDigest): MutableList<String>
}

data class WeightedRateCardDigestMaximumRangeRule(
    var min: Double,
    var max: Double,
) : WeightedRateCardDigestRule {
    override fun check(item: WeightedRateCardDigest): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardDigestTitleLengthRule(var maxLength: Int) : WeightedRateCardDigestRule {
    override fun check(item: WeightedRateCardDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardDigestRequiredTitleRule(var message: String) : WeightedRateCardDigestRule {
    override fun check(item: WeightedRateCardDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardDigestAllowedNameRule(
    var allowed: MutableList<String>,
) : WeightedRateCardDigestRule {
    override fun check(item: WeightedRateCardDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted rate card digest. */
data class WeightedRateCardDigestValidator(var rules: MutableList<WeightedRateCardDigestRule>) {
    fun validate(item: WeightedRateCardDigest): MutableList<String> {
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

    fun requireValid(item: WeightedRateCardDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRateCardDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRateCardDigestValidator(): WeightedRateCardDigestValidator {
    return WeightedRateCardDigestValidator(
        mutableListOf<WeightedRateCardDigestRule>(
            WeightedRateCardDigestMaximumRangeRule(6.0, 4312.3),
            WeightedRateCardDigestTitleLengthRule(1000),
            WeightedRateCardDigestRequiredTitleRule("weighted rate card digest needs an entry"),
            WeightedRateCardDigestAllowedNameRule(mutableListOf<String>("tiered")),
        ),
    )
}
