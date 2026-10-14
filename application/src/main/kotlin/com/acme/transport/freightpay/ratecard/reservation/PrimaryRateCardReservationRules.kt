package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.kit.text.normalize

/** A rule a primary rate card reservation must pass. Check returns its problems, or none. */
interface PrimaryRateCardReservationRule {
    fun check(item: PrimaryRateCardReservation): MutableList<String>
}

data class PrimaryRateCardReservationVersionRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryRateCardReservationRule {
    override fun check(item: PrimaryRateCardReservation): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class PrimaryRateCardReservationLabelLengthRule(var maxLength: Int) : PrimaryRateCardReservationRule {
    override fun check(item: PrimaryRateCardReservation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRateCardReservationCodeLengthRule(var maxLength: Int) : PrimaryRateCardReservationRule {
    override fun check(item: PrimaryRateCardReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRateCardReservationAllowedUnitRule(
    var allowed: MutableList<String>,
) : PrimaryRateCardReservationRule {
    override fun check(item: PrimaryRateCardReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRateCardReservationApprovedGuardRule(var limit: Int) : PrimaryRateCardReservationRule {
    override fun check(item: PrimaryRateCardReservation): MutableList<String> {
        if (item.approved && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary rate card reservation. */
data class PrimaryRateCardReservationValidator(var rules: MutableList<PrimaryRateCardReservationRule>) {
    fun validate(item: PrimaryRateCardReservation): MutableList<String> {
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

    fun requireValid(item: PrimaryRateCardReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRateCardReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRateCardReservationValidator(): PrimaryRateCardReservationValidator {
    return PrimaryRateCardReservationValidator(
        mutableListOf<PrimaryRateCardReservationRule>(
            PrimaryRateCardReservationVersionRangeRule(32, 64),
            PrimaryRateCardReservationLabelLengthRule(86400),
            PrimaryRateCardReservationCodeLengthRule(64),
            PrimaryRateCardReservationAllowedUnitRule(mutableListOf<String>("manual")),
            PrimaryRateCardReservationApprovedGuardRule(255),
        ),
    )
}
