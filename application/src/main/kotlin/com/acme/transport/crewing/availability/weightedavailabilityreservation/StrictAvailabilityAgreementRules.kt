package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.text.normalize

/** A rule a strict availability agreement must pass. Check returns its problems, or none. */
interface StrictAvailabilityAgreementRule {
    fun check(item: StrictAvailabilityAgreement): MutableList<String>
}

data class StrictAvailabilityAgreementSegmentLengthRule(var maxLength: Int) : StrictAvailabilityAgreementRule {
    override fun check(item: StrictAvailabilityAgreement): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityAgreementAmountRangeRule(
    var min: Double,
    var max: Double,
) : StrictAvailabilityAgreementRule {
    override fun check(item: StrictAvailabilityAgreement): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityAgreementAllowedSegmentRule(
    var allowed: MutableList<String>,
) : StrictAvailabilityAgreementRule {
    override fun check(item: StrictAvailabilityAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict availability agreement. */
data class StrictAvailabilityAgreementValidator(var rules: MutableList<StrictAvailabilityAgreementRule>) {
    fun validate(item: StrictAvailabilityAgreement): MutableList<String> {
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

    fun requireValid(item: StrictAvailabilityAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAvailabilityAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAvailabilityAgreementValidator(): StrictAvailabilityAgreementValidator {
    return StrictAvailabilityAgreementValidator(
        mutableListOf<StrictAvailabilityAgreementRule>(
            StrictAvailabilityAgreementSegmentLengthRule(1000),
            StrictAvailabilityAgreementAmountRangeRule(3.1, 4918.8),
            StrictAvailabilityAgreementAllowedSegmentRule(mutableListOf<String>("primary")),
        ),
    )
}
