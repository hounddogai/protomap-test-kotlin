package com.acme.transport.lastmile.locker.mapping

/** A rule a weighted locker exception must pass. Check returns its problems, or none. */
interface WeightedLockerExceptionRule {
    fun check(item: WeightedLockerException): MutableList<String>
}

data class WeightedLockerExceptionDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : WeightedLockerExceptionRule {
    override fun check(item: WeightedLockerException): MutableList<String> {
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

data class WeightedLockerExceptionEndsAtRangeRule(var min: Int, var max: Int) : WeightedLockerExceptionRule {
    override fun check(item: WeightedLockerException): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLockerExceptionTrailerIdLengthRule(var maxLength: Int) : WeightedLockerExceptionRule {
    override fun check(item: WeightedLockerException): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted locker exception. */
data class WeightedLockerExceptionValidator(var rules: MutableList<WeightedLockerExceptionRule>) {
    fun validate(item: WeightedLockerException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedLockerException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLockerException>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLockerExceptionValidator(): WeightedLockerExceptionValidator {
    return WeightedLockerExceptionValidator(
        mutableListOf<WeightedLockerExceptionRule>(
            WeightedLockerExceptionDistanceKmRangeRule(5.6, 4643.2),
            WeightedLockerExceptionEndsAtRangeRule(80, 1000),
            WeightedLockerExceptionTrailerIdLengthRule(1000),
        ),
    )
}
