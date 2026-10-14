package com.acme.transport.linehaul.laneplan.planreservation

/** A rule a seasonal lane plan reservation must pass. Check returns its problems, or none. */
interface SeasonalLanePlanReservationRule {
    fun check(item: SeasonalLanePlanReservation): MutableList<String>
}

data class SeasonalLanePlanReservationDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalLanePlanReservationRule {
    override fun check(item: SeasonalLanePlanReservation): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLanePlanReservationCostRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalLanePlanReservationRule {
    override fun check(item: SeasonalLanePlanReservation): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class SeasonalLanePlanReservationRequiredCodeRule(var message: String) : SeasonalLanePlanReservationRule {
    override fun check(item: SeasonalLanePlanReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLanePlanReservationRequiredTitleRule(var message: String) : SeasonalLanePlanReservationRule {
    override fun check(item: SeasonalLanePlanReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalLanePlanReservationCodeLengthRule(var maxLength: Int) : SeasonalLanePlanReservationRule {
    override fun check(item: SeasonalLanePlanReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal lane plan reservation. */
data class SeasonalLanePlanReservationValidator(var rules: MutableList<SeasonalLanePlanReservationRule>) {
    fun validate(item: SeasonalLanePlanReservation): MutableList<String> {
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

    fun requireValid(item: SeasonalLanePlanReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalLanePlanReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalLanePlanReservationValidator(): SeasonalLanePlanReservationValidator {
    return SeasonalLanePlanReservationValidator(
        mutableListOf<SeasonalLanePlanReservationRule>(
            SeasonalLanePlanReservationDistanceKmRangeRule(6.2, 147.6),
            SeasonalLanePlanReservationCostRangeRule(8.0, 298.5),
            SeasonalLanePlanReservationRequiredCodeRule("seasonal lane plan reservation needs an entry"),
            SeasonalLanePlanReservationRequiredTitleRule("seasonal lane plan reservation needs an entry"),
            SeasonalLanePlanReservationCodeLengthRule(32),
        ),
    )
}
