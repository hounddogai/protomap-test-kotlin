package com.acme.transport.linehaul.capacity.capacityreservation

/** A rule a tiered capacity reservation must pass. Check returns its problems, or none. */
interface TieredCapacityReservationRule {
    fun check(item: TieredCapacityReservation): MutableList<String>
}

data class TieredCapacityReservationCodePrefixRule(var prefix: String) : TieredCapacityReservationRule {
    override fun check(item: TieredCapacityReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredCapacityReservationContactEmailLengthRule(var maxLength: Int) : TieredCapacityReservationRule {
    override fun check(item: TieredCapacityReservation): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCapacityReservationRequiredLabelRule(var message: String) : TieredCapacityReservationRule {
    override fun check(item: TieredCapacityReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered capacity reservation. */
data class TieredCapacityReservationValidator(var rules: MutableList<TieredCapacityReservationRule>) {
    fun validate(item: TieredCapacityReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCapacityReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCapacityReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCapacityReservationValidator(): TieredCapacityReservationValidator {
    return TieredCapacityReservationValidator(
        mutableListOf<TieredCapacityReservationRule>(
            TieredCapacityReservationCodePrefixRule("REF"),
            TieredCapacityReservationContactEmailLengthRule(3600),
            TieredCapacityReservationRequiredLabelRule("tiered capacity reservation needs an entry"),
        ),
    )
}
