package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.text.normalize

/** A rule a legacy lease return reservation must pass. Check returns its problems, or none. */
interface LegacyLeaseReturnReservationRule {
    fun check(item: LegacyLeaseReturnReservation): MutableList<String>
}

data class LegacyLeaseReturnReservationAllowedChecksumRule(
    var allowed: MutableList<String>,
) : LegacyLeaseReturnReservationRule {
    override fun check(item: LegacyLeaseReturnReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnReservationRequiredNameRule(
    var message: String,
) : LegacyLeaseReturnReservationRule {
    override fun check(item: LegacyLeaseReturnReservation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnReservationRequiredChecksumRule(
    var message: String,
) : LegacyLeaseReturnReservationRule {
    override fun check(item: LegacyLeaseReturnReservation): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLeaseReturnReservationRatioRangeRule(
    var min: Double,
    var max: Double,
) : LegacyLeaseReturnReservationRule {
    override fun check(item: LegacyLeaseReturnReservation): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy lease return reservation. */
data class LegacyLeaseReturnReservationValidator(var rules: MutableList<LegacyLeaseReturnReservationRule>) {
    fun validate(item: LegacyLeaseReturnReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyLeaseReturnReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLeaseReturnReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLeaseReturnReservationValidator(): LegacyLeaseReturnReservationValidator {
    return LegacyLeaseReturnReservationValidator(
        mutableListOf<LegacyLeaseReturnReservationRule>(
            LegacyLeaseReturnReservationAllowedChecksumRule(
                mutableListOf<String>("regional", "tiered", "primary"),
            ),
            LegacyLeaseReturnReservationRequiredNameRule("legacy lease return reservation needs this field"),
            LegacyLeaseReturnReservationRequiredChecksumRule("legacy lease return reservation needs an entry"),
            LegacyLeaseReturnReservationRatioRangeRule(9.5, 4648.3),
        ),
    )
}
