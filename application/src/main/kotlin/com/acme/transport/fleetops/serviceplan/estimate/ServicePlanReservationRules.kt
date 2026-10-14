package com.acme.transport.fleetops.serviceplan.estimate

/** A rule a service plan reservation must pass. Check returns its problems, or none. */
interface ServicePlanReservationRule {
    fun check(item: ServicePlanReservation): MutableList<String>
}

data class ServicePlanReservationCodeLengthRule(var maxLength: Int) : ServicePlanReservationRule {
    override fun check(item: ServicePlanReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanReservationNameLengthRule(var maxLength: Int) : ServicePlanReservationRule {
    override fun check(item: ServicePlanReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanReservationCodePrefixRule(var prefix: String) : ServicePlanReservationRule {
    override fun check(item: ServicePlanReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanReservationDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ServicePlanReservationRule {
    override fun check(item: ServicePlanReservation): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class ServicePlanReservationRequiredCodeRule(var message: String) : ServicePlanReservationRule {
    override fun check(item: ServicePlanReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan reservation. */
data class ServicePlanReservationValidator(var rules: MutableList<ServicePlanReservationRule>) {
    fun validate(item: ServicePlanReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanReservationValidator(): ServicePlanReservationValidator {
    return ServicePlanReservationValidator(
        mutableListOf<ServicePlanReservationRule>(
            ServicePlanReservationCodeLengthRule(1000),
            ServicePlanReservationNameLengthRule(80),
            ServicePlanReservationCodePrefixRule("REF"),
            ServicePlanReservationDurationMinutesRangeRule(32, 1000),
            ServicePlanReservationRequiredCodeRule("service plan reservation needs an entry"),
        ),
    )
}
