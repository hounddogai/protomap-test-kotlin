package com.acme.transport.network.cutoff.cutofftarget

/** A rule a regional cutoff reservation must pass. Check returns its problems, or none. */
interface RegionalCutoffReservationRule {
    fun check(item: RegionalCutoffReservation): MutableList<String>
}

data class RegionalCutoffReservationExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCutoffReservationRule {
    override fun check(item: RegionalCutoffReservation): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class RegionalCutoffReservationCodePrefixRule(var prefix: String) : RegionalCutoffReservationRule {
    override fun check(item: RegionalCutoffReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffReservationRequiredTimezoneRule(var message: String) : RegionalCutoffReservationRule {
    override fun check(item: RegionalCutoffReservation): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional cutoff reservation. */
data class RegionalCutoffReservationValidator(var rules: MutableList<RegionalCutoffReservationRule>) {
    fun validate(item: RegionalCutoffReservation): MutableList<String> {
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

    fun requireValid(item: RegionalCutoffReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCutoffReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCutoffReservationValidator(): RegionalCutoffReservationValidator {
    return RegionalCutoffReservationValidator(
        mutableListOf<RegionalCutoffReservationRule>(
            RegionalCutoffReservationExpiresAtRangeRule(16, 86400),
            RegionalCutoffReservationCodePrefixRule("REF"),
            RegionalCutoffReservationRequiredTimezoneRule("regional cutoff reservation needs this field"),
        ),
    )
}
