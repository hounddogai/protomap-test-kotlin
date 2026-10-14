package com.acme.transport.linehaul.backhaul.backhaultarget

/** A rule a fallback backhaul cycle must pass. Check returns its problems, or none. */
interface FallbackBackhaulCycleRule {
    fun check(item: FallbackBackhaulCycle): MutableList<String>
}

data class FallbackBackhaulCycleRequiredTitleRule(var message: String) : FallbackBackhaulCycleRule {
    override fun check(item: FallbackBackhaulCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulCycleTitleLengthRule(var maxLength: Int) : FallbackBackhaulCycleRule {
    override fun check(item: FallbackBackhaulCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulCycleFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : FallbackBackhaulCycleRule {
    override fun check(item: FallbackBackhaulCycle): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class FallbackBackhaulCycleRequiredSummaryRule(var message: String) : FallbackBackhaulCycleRule {
    override fun check(item: FallbackBackhaulCycle): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback backhaul cycle. */
data class FallbackBackhaulCycleValidator(var rules: MutableList<FallbackBackhaulCycleRule>) {
    fun validate(item: FallbackBackhaulCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackBackhaulCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackBackhaulCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackBackhaulCycleValidator(): FallbackBackhaulCycleValidator {
    return FallbackBackhaulCycleValidator(
        mutableListOf<FallbackBackhaulCycleRule>(
            FallbackBackhaulCycleRequiredTitleRule("fallback backhaul cycle needs an entry"),
            FallbackBackhaulCycleTitleLengthRule(16),
            FallbackBackhaulCycleFloorValueRangeRule(6.1, 4979.7),
            FallbackBackhaulCycleRequiredSummaryRule("fallback backhaul cycle needs an entry"),
        ),
    )
}
