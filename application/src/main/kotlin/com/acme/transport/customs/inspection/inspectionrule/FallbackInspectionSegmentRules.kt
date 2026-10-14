package com.acme.transport.customs.inspection.inspectionrule

/** A rule a fallback inspection segment must pass. Check returns its problems, or none. */
interface FallbackInspectionSegmentRule {
    fun check(item: FallbackInspectionSegment): MutableList<String>
}

data class FallbackInspectionSegmentRequiredTitleRule(var message: String) : FallbackInspectionSegmentRule {
    override fun check(item: FallbackInspectionSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionSegmentStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackInspectionSegmentRule {
    override fun check(item: FallbackInspectionSegment): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionSegmentRequiredSegmentRule(var message: String) : FallbackInspectionSegmentRule {
    override fun check(item: FallbackInspectionSegment): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionSegmentBudgetRangeRule(
    var min: Double,
    var max: Double,
) : FallbackInspectionSegmentRule {
    override fun check(item: FallbackInspectionSegment): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback inspection segment. */
data class FallbackInspectionSegmentValidator(var rules: MutableList<FallbackInspectionSegmentRule>) {
    fun validate(item: FallbackInspectionSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackInspectionSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackInspectionSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackInspectionSegmentValidator(): FallbackInspectionSegmentValidator {
    return FallbackInspectionSegmentValidator(
        mutableListOf<FallbackInspectionSegmentRule>(
            FallbackInspectionSegmentRequiredTitleRule("fallback inspection segment needs an entry"),
            FallbackInspectionSegmentStartsAtRangeRule(16, 1000),
            FallbackInspectionSegmentRequiredSegmentRule("fallback inspection segment needs an entry"),
            FallbackInspectionSegmentBudgetRangeRule(0.9, 2818.1),
        ),
    )
}
