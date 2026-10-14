package com.acme.transport.emissions.idling.proposal

/** A rule a rolling idling segment must pass. Check returns its problems, or none. */
interface RollingIdlingSegmentRule {
    fun check(item: RollingIdlingSegment): MutableList<String>
}

data class RollingIdlingSegmentRequiredUnitRule(var message: String) : RollingIdlingSegmentRule {
    override fun check(item: RollingIdlingSegment): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingIdlingSegmentRatioRangeRule(var min: Double, var max: Double) : RollingIdlingSegmentRule {
    override fun check(item: RollingIdlingSegment): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class RollingIdlingSegmentRequiredContactEmailRule(var message: String) : RollingIdlingSegmentRule {
    override fun check(item: RollingIdlingSegment): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling idling segment. */
data class RollingIdlingSegmentValidator(var rules: MutableList<RollingIdlingSegmentRule>) {
    fun validate(item: RollingIdlingSegment): MutableList<String> {
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

    fun requireValid(item: RollingIdlingSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingIdlingSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingIdlingSegmentValidator(): RollingIdlingSegmentValidator {
    return RollingIdlingSegmentValidator(
        mutableListOf<RollingIdlingSegmentRule>(
            RollingIdlingSegmentRequiredUnitRule("rolling idling segment needs this field"),
            RollingIdlingSegmentRatioRangeRule(0.4, 4181.4),
            RollingIdlingSegmentRequiredContactEmailRule("rolling idling segment needs a value"),
        ),
    )
}
