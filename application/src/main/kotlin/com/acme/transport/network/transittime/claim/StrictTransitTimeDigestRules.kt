package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.normalize

/** A rule a strict transit time digest must pass. Check returns its problems, or none. */
interface StrictTransitTimeDigestRule {
    fun check(item: StrictTransitTimeDigest): MutableList<String>
}

data class StrictTransitTimeDigestAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictTransitTimeDigestRule {
    override fun check(item: StrictTransitTimeDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeDigestRequiredShiftCodeRule(var message: String) : StrictTransitTimeDigestRule {
    override fun check(item: StrictTransitTimeDigest): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeDigestRequiredCodeRule(var message: String) : StrictTransitTimeDigestRule {
    override fun check(item: StrictTransitTimeDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTransitTimeDigestFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : StrictTransitTimeDigestRule {
    override fun check(item: StrictTransitTimeDigest): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class StrictTransitTimeDigestShiftCodeLengthRule(var maxLength: Int) : StrictTransitTimeDigestRule {
    override fun check(item: StrictTransitTimeDigest): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict transit time digest. */
data class StrictTransitTimeDigestValidator(var rules: MutableList<StrictTransitTimeDigestRule>) {
    fun validate(item: StrictTransitTimeDigest): MutableList<String> {
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

    fun requireValid(item: StrictTransitTimeDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTransitTimeDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTransitTimeDigestValidator(): StrictTransitTimeDigestValidator {
    return StrictTransitTimeDigestValidator(
        mutableListOf<StrictTransitTimeDigestRule>(
            StrictTransitTimeDigestAllowedCodeRule(mutableListOf<String>("default", "batched")),
            StrictTransitTimeDigestRequiredShiftCodeRule("strict transit time digest needs an entry"),
            StrictTransitTimeDigestRequiredCodeRule("strict transit time digest needs this field"),
            StrictTransitTimeDigestFuelLitersRangeRule(1.6, 1920.6),
            StrictTransitTimeDigestShiftCodeLengthRule(3600),
        ),
    )
}
