package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.text.normalize

/** A rule a global capacity reservation must pass. Check returns its problems, or none. */
interface GlobalCapacityReservationRule {
    fun check(item: GlobalCapacityReservation): MutableList<String>
}

data class GlobalCapacityReservationAllowedNameRule(
    var allowed: MutableList<String>,
) : GlobalCapacityReservationRule {
    override fun check(item: GlobalCapacityReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCapacityReservationRegionLengthRule(var maxLength: Int) : GlobalCapacityReservationRule {
    override fun check(item: GlobalCapacityReservation): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCapacityReservationApprovedGuardRule(var limit: Int) : GlobalCapacityReservationRule {
    override fun check(item: GlobalCapacityReservation): MutableList<String> {
        if (item.approved && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCapacityReservationCodePrefixRule(var prefix: String) : GlobalCapacityReservationRule {
    override fun check(item: GlobalCapacityReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global capacity reservation. */
data class GlobalCapacityReservationValidator(var rules: MutableList<GlobalCapacityReservationRule>) {
    fun validate(item: GlobalCapacityReservation): MutableList<String> {
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

    fun requireValid(item: GlobalCapacityReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCapacityReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCapacityReservationValidator(): GlobalCapacityReservationValidator {
    return GlobalCapacityReservationValidator(
        mutableListOf<GlobalCapacityReservationRule>(
            GlobalCapacityReservationAllowedNameRule(mutableListOf<String>("regional")),
            GlobalCapacityReservationRegionLengthRule(32),
            GlobalCapacityReservationApprovedGuardRule(64),
            GlobalCapacityReservationCodePrefixRule("Q"),
        ),
    )
}
