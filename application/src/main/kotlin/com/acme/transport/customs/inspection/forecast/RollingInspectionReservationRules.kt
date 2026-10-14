package com.acme.transport.customs.inspection.forecast

/** A rule a rolling inspection reservation must pass. Check returns its problems, or none. */
interface RollingInspectionReservationRule {
    fun check(item: RollingInspectionReservation): MutableList<String>
}

data class RollingInspectionReservationNameLengthRule(var maxLength: Int) : RollingInspectionReservationRule {
    override fun check(item: RollingInspectionReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionReservationSequenceRangeRule(
    var min: Int,
    var max: Int,
) : RollingInspectionReservationRule {
    override fun check(item: RollingInspectionReservation): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionReservationCategoryLengthRule(
    var maxLength: Int,
) : RollingInspectionReservationRule {
    override fun check(item: RollingInspectionReservation): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionReservationRequiredCategoryRule(
    var message: String,
) : RollingInspectionReservationRule {
    override fun check(item: RollingInspectionReservation): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionReservationScoreRangeRule(
    var min: Double,
    var max: Double,
) : RollingInspectionReservationRule {
    override fun check(item: RollingInspectionReservation): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionReservationFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RollingInspectionReservationRule {
    override fun check(item: RollingInspectionReservation): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling inspection reservation. */
data class RollingInspectionReservationValidator(var rules: MutableList<RollingInspectionReservationRule>) {
    fun validate(item: RollingInspectionReservation): MutableList<String> {
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

    fun requireValid(item: RollingInspectionReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingInspectionReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingInspectionReservationValidator(): RollingInspectionReservationValidator {
    return RollingInspectionReservationValidator(
        mutableListOf<RollingInspectionReservationRule>(
            RollingInspectionReservationNameLengthRule(16),
            RollingInspectionReservationSequenceRangeRule(64, 80),
            RollingInspectionReservationCategoryLengthRule(32),
            RollingInspectionReservationRequiredCategoryRule("rolling inspection reservation needs an entry"),
            RollingInspectionReservationScoreRangeRule(7.9, 2216.1),
            RollingInspectionReservationFuelLitersRangeRule(5.9, 2008.4),
        ),
    )
}
