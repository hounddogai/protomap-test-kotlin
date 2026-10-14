package com.acme.transport.linehaul.capacity.exception

/** A rule a seasonal capacity exception must pass. Check returns its problems, or none. */
interface SeasonalCapacityExceptionRule {
    fun check(item: SeasonalCapacityException): MutableList<String>
}

data class SeasonalCapacityExceptionRequiredTimezoneRule(var message: String) : SeasonalCapacityExceptionRule {
    override fun check(item: SeasonalCapacityException): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityExceptionDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalCapacityExceptionRule {
    override fun check(item: SeasonalCapacityException): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class SeasonalCapacityExceptionLimitRangeRule(var min: Int, var max: Int) : SeasonalCapacityExceptionRule {
    override fun check(item: SeasonalCapacityException): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class SeasonalCapacityExceptionSourceLengthRule(var maxLength: Int) : SeasonalCapacityExceptionRule {
    override fun check(item: SeasonalCapacityException): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityExceptionRequiredCodeRule(var message: String) : SeasonalCapacityExceptionRule {
    override fun check(item: SeasonalCapacityException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityExceptionRequiredSourceRule(var message: String) : SeasonalCapacityExceptionRule {
    override fun check(item: SeasonalCapacityException): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal capacity exception. */
data class SeasonalCapacityExceptionValidator(var rules: MutableList<SeasonalCapacityExceptionRule>) {
    fun validate(item: SeasonalCapacityException): MutableList<String> {
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

    fun requireValid(item: SeasonalCapacityException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCapacityException>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCapacityExceptionValidator(): SeasonalCapacityExceptionValidator {
    return SeasonalCapacityExceptionValidator(
        mutableListOf<SeasonalCapacityExceptionRule>(
            SeasonalCapacityExceptionRequiredTimezoneRule("seasonal capacity exception needs this field"),
            SeasonalCapacityExceptionDistanceKmRangeRule(3.6, 201.3),
            SeasonalCapacityExceptionLimitRangeRule(64, 120),
            SeasonalCapacityExceptionSourceLengthRule(80),
            SeasonalCapacityExceptionRequiredCodeRule("seasonal capacity exception needs this field"),
            SeasonalCapacityExceptionRequiredSourceRule("seasonal capacity exception needs an entry"),
        ),
    )
}
