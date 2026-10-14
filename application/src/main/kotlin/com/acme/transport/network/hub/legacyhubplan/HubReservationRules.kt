package com.acme.transport.network.hub.legacyhubplan

/** A rule a hub reservation must pass. Check returns its problems, or none. */
interface HubReservationRule {
    fun check(item: HubReservation): MutableList<String>
}

data class HubReservationRequiredCodeRule(var message: String) : HubReservationRule {
    override fun check(item: HubReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubReservationRequiredDepotRule(var message: String) : HubReservationRule {
    override fun check(item: HubReservation): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubReservationWeightRangeRule(var min: Double, var max: Double) : HubReservationRule {
    override fun check(item: HubReservation): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class HubReservationCodePrefixRule(var prefix: String) : HubReservationRule {
    override fun check(item: HubReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub reservation. */
data class HubReservationValidator(var rules: MutableList<HubReservationRule>) {
    fun validate(item: HubReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultHubReservationValidator(): HubReservationValidator {
    return HubReservationValidator(
        mutableListOf<HubReservationRule>(
            HubReservationRequiredCodeRule("hub reservation needs a value"),
            HubReservationRequiredDepotRule("hub reservation needs a value"),
            HubReservationWeightRangeRule(2.6, 3640.7),
            HubReservationCodePrefixRule("Q"),
        ),
    )
}
