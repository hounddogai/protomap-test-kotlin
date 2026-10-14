package com.acme.transport.network.servicearea.draft

/** A rule a legacy service area cycle must pass. Check returns its problems, or none. */
interface LegacyServiceAreaCycleRule {
    fun check(item: LegacyServiceAreaCycle): MutableList<String>
}

data class LegacyServiceAreaCycleRequiredRegionRule(var message: String) : LegacyServiceAreaCycleRule {
    override fun check(item: LegacyServiceAreaCycle): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyServiceAreaCycleRequiredSegmentRule(var message: String) : LegacyServiceAreaCycleRule {
    override fun check(item: LegacyServiceAreaCycle): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyServiceAreaCycleRequiredTitleRule(var message: String) : LegacyServiceAreaCycleRule {
    override fun check(item: LegacyServiceAreaCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyServiceAreaCycleAttemptsRangeRule(var min: Int, var max: Int) : LegacyServiceAreaCycleRule {
    override fun check(item: LegacyServiceAreaCycle): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServiceAreaCycleDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : LegacyServiceAreaCycleRule {
    override fun check(item: LegacyServiceAreaCycle): MutableList<String> {
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

data class LegacyServiceAreaCyclePriorityRangeRule(var min: Int, var max: Int) : LegacyServiceAreaCycleRule {
    override fun check(item: LegacyServiceAreaCycle): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy service area cycle. */
data class LegacyServiceAreaCycleValidator(var rules: MutableList<LegacyServiceAreaCycleRule>) {
    fun validate(item: LegacyServiceAreaCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyServiceAreaCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyServiceAreaCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyServiceAreaCycleValidator(): LegacyServiceAreaCycleValidator {
    return LegacyServiceAreaCycleValidator(
        mutableListOf<LegacyServiceAreaCycleRule>(
            LegacyServiceAreaCycleRequiredRegionRule("legacy service area cycle needs this field"),
            LegacyServiceAreaCycleRequiredSegmentRule("legacy service area cycle needs an entry"),
            LegacyServiceAreaCycleRequiredTitleRule("legacy service area cycle needs an entry"),
            LegacyServiceAreaCycleAttemptsRangeRule(32, 80),
            LegacyServiceAreaCycleDistanceKmRangeRule(4.4, 3839.4),
            LegacyServiceAreaCyclePriorityRangeRule(80, 3600),
        ),
    )
}
