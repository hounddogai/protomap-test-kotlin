package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.text.normalize

/** A rule a strict lease return reservation must pass. Check returns its problems, or none. */
interface StrictLeaseReturnReservationRule {
    fun check(item: StrictLeaseReturnReservation): MutableList<String>
}

data class StrictLeaseReturnReservationRequiredCategoryRule(
    var message: String,
) : StrictLeaseReturnReservationRule {
    override fun check(item: StrictLeaseReturnReservation): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictLeaseReturnReservationUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : StrictLeaseReturnReservationRule {
    override fun check(item: StrictLeaseReturnReservation): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictLeaseReturnReservationAllowedNameRule(
    var allowed: MutableList<String>,
) : StrictLeaseReturnReservationRule {
    override fun check(item: StrictLeaseReturnReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class StrictLeaseReturnReservationCodeLengthRule(var maxLength: Int) : StrictLeaseReturnReservationRule {
    override fun check(item: StrictLeaseReturnReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict lease return reservation. */
data class StrictLeaseReturnReservationValidator(var rules: MutableList<StrictLeaseReturnReservationRule>) {
    fun validate(item: StrictLeaseReturnReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictLeaseReturnReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictLeaseReturnReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictLeaseReturnReservationValidator(): StrictLeaseReturnReservationValidator {
    return StrictLeaseReturnReservationValidator(
        mutableListOf<StrictLeaseReturnReservationRule>(
            StrictLeaseReturnReservationRequiredCategoryRule("strict lease return reservation needs a value"),
            StrictLeaseReturnReservationUpdatedAtRangeRule(16, 1000),
            StrictLeaseReturnReservationAllowedNameRule(mutableListOf<String>("tiered", "global", "default")),
            StrictLeaseReturnReservationCodeLengthRule(3600),
        ),
    )
}
