package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.normalize

/** A rule a legacy trailer yard claim must pass. Check returns its problems, or none. */
interface LegacyTrailerYardClaimRule {
    fun check(item: LegacyTrailerYardClaim): MutableList<String>
}

data class LegacyTrailerYardClaimAllowedCarrierRule(
    var allowed: MutableList<String>,
) : LegacyTrailerYardClaimRule {
    override fun check(item: LegacyTrailerYardClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardClaimCodeLengthRule(var maxLength: Int) : LegacyTrailerYardClaimRule {
    override fun check(item: LegacyTrailerYardClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardClaimDescriptionLengthRule(var maxLength: Int) : LegacyTrailerYardClaimRule {
    override fun check(item: LegacyTrailerYardClaim): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardClaimLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : LegacyTrailerYardClaimRule {
    override fun check(item: LegacyTrailerYardClaim): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class LegacyTrailerYardClaimThresholdRangeRule(
    var min: Double,
    var max: Double,
) : LegacyTrailerYardClaimRule {
    override fun check(item: LegacyTrailerYardClaim): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy trailer yard claim. */
data class LegacyTrailerYardClaimValidator(var rules: MutableList<LegacyTrailerYardClaimRule>) {
    fun validate(item: LegacyTrailerYardClaim): MutableList<String> {
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

    fun requireValid(item: LegacyTrailerYardClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTrailerYardClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTrailerYardClaimValidator(): LegacyTrailerYardClaimValidator {
    return LegacyTrailerYardClaimValidator(
        mutableListOf<LegacyTrailerYardClaimRule>(
            LegacyTrailerYardClaimAllowedCarrierRule(mutableListOf<String>("strict")),
            LegacyTrailerYardClaimCodeLengthRule(1000),
            LegacyTrailerYardClaimDescriptionLengthRule(255),
            LegacyTrailerYardClaimLoadKgRangeRule(0.2, 3130.5),
            LegacyTrailerYardClaimThresholdRangeRule(6.9, 3683.3),
        ),
    )
}
