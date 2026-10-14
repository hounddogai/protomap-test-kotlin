package com.acme.transport.linehaul.capacity.milestone

/** A rule a lenient capacity reservation must pass. Check returns its problems, or none. */
interface LenientCapacityReservationRule {
    fun check(item: LenientCapacityReservation): MutableList<String>
}

data class LenientCapacityReservationWeightRangeRule(
    var min: Double,
    var max: Double,
) : LenientCapacityReservationRule {
    override fun check(item: LenientCapacityReservation): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityReservationBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : LenientCapacityReservationRule {
    override fun check(item: LenientCapacityReservation): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class LenientCapacityReservationCodePrefixRule(var prefix: String) : LenientCapacityReservationRule {
    override fun check(item: LenientCapacityReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientCapacityReservationRequiredDepotRule(var message: String) : LenientCapacityReservationRule {
    override fun check(item: LenientCapacityReservation): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient capacity reservation. */
data class LenientCapacityReservationValidator(var rules: MutableList<LenientCapacityReservationRule>) {
    fun validate(item: LenientCapacityReservation): MutableList<String> {
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

    fun requireValid(item: LenientCapacityReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCapacityReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCapacityReservationValidator(): LenientCapacityReservationValidator {
    return LenientCapacityReservationValidator(
        mutableListOf<LenientCapacityReservationRule>(
            LenientCapacityReservationWeightRangeRule(6.1, 4540.2),
            LenientCapacityReservationBatchSizeRangeRule(64, 255),
            LenientCapacityReservationCodePrefixRule("OPS"),
            LenientCapacityReservationRequiredDepotRule("lenient capacity reservation needs a value"),
        ),
    )
}
