package com.acme.transport.network.servicearea.quota

/** A rule a service area reservation must pass. Check returns its problems, or none. */
interface ServiceAreaReservationRule {
    fun check(item: ServiceAreaReservation): MutableList<String>
}

data class ServiceAreaReservationRequiredCodeRule(var message: String) : ServiceAreaReservationRule {
    override fun check(item: ServiceAreaReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaReservationContactEmailLengthRule(var maxLength: Int) : ServiceAreaReservationRule {
    override fun check(item: ServiceAreaReservation): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaReservationCodePrefixRule(var prefix: String) : ServiceAreaReservationRule {
    override fun check(item: ServiceAreaReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area reservation. */
data class ServiceAreaReservationValidator(var rules: MutableList<ServiceAreaReservationRule>) {
    fun validate(item: ServiceAreaReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaReservationValidator(): ServiceAreaReservationValidator {
    return ServiceAreaReservationValidator(
        mutableListOf<ServiceAreaReservationRule>(
            ServiceAreaReservationRequiredCodeRule("service area reservation needs a value"),
            ServiceAreaReservationContactEmailLengthRule(3600),
            ServiceAreaReservationCodePrefixRule("X"),
        ),
    )
}
