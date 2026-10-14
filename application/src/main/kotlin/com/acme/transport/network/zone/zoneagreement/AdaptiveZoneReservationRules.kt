package com.acme.transport.network.zone.zoneagreement

/** A rule a adaptive zone reservation must pass. Check returns its problems, or none. */
interface AdaptiveZoneReservationRule {
    fun check(item: AdaptiveZoneReservation): MutableList<String>
}

data class AdaptiveZoneReservationCodePrefixRule(var prefix: String) : AdaptiveZoneReservationRule {
    override fun check(item: AdaptiveZoneReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneReservationCodeLengthRule(var maxLength: Int) : AdaptiveZoneReservationRule {
    override fun check(item: AdaptiveZoneReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneReservationRequiredRouteCodeRule(var message: String) : AdaptiveZoneReservationRule {
    override fun check(item: AdaptiveZoneReservation): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive zone reservation. */
data class AdaptiveZoneReservationValidator(var rules: MutableList<AdaptiveZoneReservationRule>) {
    fun validate(item: AdaptiveZoneReservation): MutableList<String> {
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

    fun requireValid(item: AdaptiveZoneReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveZoneReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveZoneReservationValidator(): AdaptiveZoneReservationValidator {
    return AdaptiveZoneReservationValidator(
        mutableListOf<AdaptiveZoneReservationRule>(
            AdaptiveZoneReservationCodePrefixRule("ACM"),
            AdaptiveZoneReservationCodeLengthRule(255),
            AdaptiveZoneReservationRequiredRouteCodeRule("adaptive zone reservation needs an entry"),
        ),
    )
}
