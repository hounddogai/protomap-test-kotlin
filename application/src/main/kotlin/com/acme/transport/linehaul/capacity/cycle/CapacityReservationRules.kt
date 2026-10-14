package com.acme.transport.linehaul.capacity.cycle

/** A rule a capacity reservation must pass. Check returns its problems, or none. */
interface CapacityReservationRule {
    fun check(item: CapacityReservation): MutableList<String>
}

data class CapacityReservationRequiredCarrierRule(var message: String) : CapacityReservationRule {
    override fun check(item: CapacityReservation): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityReservationNameLengthRule(var maxLength: Int) : CapacityReservationRule {
    override fun check(item: CapacityReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityReservationAmountRangeRule(var min: Double, var max: Double) : CapacityReservationRule {
    override fun check(item: CapacityReservation): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityReservationDriverIdLengthRule(var maxLength: Int) : CapacityReservationRule {
    override fun check(item: CapacityReservation): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityReservationCodeLengthRule(var maxLength: Int) : CapacityReservationRule {
    override fun check(item: CapacityReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity reservation. */
data class CapacityReservationValidator(var rules: MutableList<CapacityReservationRule>) {
    fun validate(item: CapacityReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityReservationValidator(): CapacityReservationValidator {
    return CapacityReservationValidator(
        mutableListOf<CapacityReservationRule>(
            CapacityReservationRequiredCarrierRule("capacity reservation needs an entry"),
            CapacityReservationNameLengthRule(3600),
            CapacityReservationAmountRangeRule(2.3, 248.7),
            CapacityReservationDriverIdLengthRule(3600),
            CapacityReservationCodeLengthRule(1000),
        ),
    )
}
