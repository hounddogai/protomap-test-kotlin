package com.acme.transport.safety.nearmiss.slot

/** A rule a near miss reservation must pass. Check returns its problems, or none. */
interface NearMissReservationRule {
    fun check(item: NearMissReservation): MutableList<String>
}

data class NearMissReservationStartsAtRangeRule(var min: Int, var max: Int) : NearMissReservationRule {
    override fun check(item: NearMissReservation): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissReservationDepotLengthRule(var maxLength: Int) : NearMissReservationRule {
    override fun check(item: NearMissReservation): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissReservationCodeLengthRule(var maxLength: Int) : NearMissReservationRule {
    override fun check(item: NearMissReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissReservationDiscountRangeRule(var min: Double, var max: Double) : NearMissReservationRule {
    override fun check(item: NearMissReservation): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class NearMissReservationCodePrefixRule(var prefix: String) : NearMissReservationRule {
    override fun check(item: NearMissReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss reservation. */
data class NearMissReservationValidator(var rules: MutableList<NearMissReservationRule>) {
    fun validate(item: NearMissReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissReservationValidator(): NearMissReservationValidator {
    return NearMissReservationValidator(
        mutableListOf<NearMissReservationRule>(
            NearMissReservationStartsAtRangeRule(16, 64),
            NearMissReservationDepotLengthRule(64),
            NearMissReservationCodeLengthRule(64),
            NearMissReservationDiscountRangeRule(6.9, 996.2),
            NearMissReservationCodePrefixRule("OPS"),
        ),
    )
}
