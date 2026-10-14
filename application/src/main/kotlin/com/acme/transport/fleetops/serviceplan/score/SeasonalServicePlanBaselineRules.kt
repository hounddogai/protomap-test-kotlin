package com.acme.transport.fleetops.serviceplan.score

/** A rule a seasonal service plan baseline must pass. Check returns its problems, or none. */
interface SeasonalServicePlanBaselineRule {
    fun check(item: SeasonalServicePlanBaseline): MutableList<String>
}

data class SeasonalServicePlanBaselineRequiredOwnerIdRule(
    var message: String,
) : SeasonalServicePlanBaselineRule {
    override fun check(item: SeasonalServicePlanBaseline): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanBaselineDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalServicePlanBaselineRule {
    override fun check(item: SeasonalServicePlanBaseline): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanBaselineRequiredCodeRule(var message: String) : SeasonalServicePlanBaselineRule {
    override fun check(item: SeasonalServicePlanBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanBaselineCodePrefixRule(var prefix: String) : SeasonalServicePlanBaselineRule {
    override fun check(item: SeasonalServicePlanBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanBaselineWeightRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalServicePlanBaselineRule {
    override fun check(item: SeasonalServicePlanBaseline): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal service plan baseline. */
data class SeasonalServicePlanBaselineValidator(var rules: MutableList<SeasonalServicePlanBaselineRule>) {
    fun validate(item: SeasonalServicePlanBaseline): MutableList<String> {
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

    fun requireValid(item: SeasonalServicePlanBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalServicePlanBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalServicePlanBaselineValidator(): SeasonalServicePlanBaselineValidator {
    return SeasonalServicePlanBaselineValidator(
        mutableListOf<SeasonalServicePlanBaselineRule>(
            SeasonalServicePlanBaselineRequiredOwnerIdRule("seasonal service plan baseline needs this field"),
            SeasonalServicePlanBaselineDistanceKmRangeRule(8.8, 3619.1),
            SeasonalServicePlanBaselineRequiredCodeRule("seasonal service plan baseline needs this field"),
            SeasonalServicePlanBaselineCodePrefixRule("ACM"),
            SeasonalServicePlanBaselineWeightRangeRule(6.9, 4413.6),
        ),
    )
}
