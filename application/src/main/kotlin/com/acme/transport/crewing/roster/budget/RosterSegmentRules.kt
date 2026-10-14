package com.acme.transport.crewing.roster.budget

/** A rule a roster segment must pass. Check returns its problems, or none. */
interface RosterSegmentRule {
    fun check(item: RosterSegment): MutableList<String>
}

data class RosterSegmentSegmentLengthRule(var maxLength: Int) : RosterSegmentRule {
    override fun check(item: RosterSegment): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterSegmentCeilingRangeRule(var min: Double, var max: Double) : RosterSegmentRule {
    override fun check(item: RosterSegment): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class RosterSegmentRequiredLabelRule(var message: String) : RosterSegmentRule {
    override fun check(item: RosterSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterSegmentDiscountRangeRule(var min: Double, var max: Double) : RosterSegmentRule {
    override fun check(item: RosterSegment): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterSegmentRequiredSegmentRule(var message: String) : RosterSegmentRule {
    override fun check(item: RosterSegment): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterSegmentLabelLengthRule(var maxLength: Int) : RosterSegmentRule {
    override fun check(item: RosterSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster segment. */
data class RosterSegmentValidator(var rules: MutableList<RosterSegmentRule>) {
    fun validate(item: RosterSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterSegmentValidator(): RosterSegmentValidator {
    return RosterSegmentValidator(
        mutableListOf<RosterSegmentRule>(
            RosterSegmentSegmentLengthRule(16),
            RosterSegmentCeilingRangeRule(4.4, 1272.4),
            RosterSegmentRequiredLabelRule("roster segment needs an entry"),
            RosterSegmentDiscountRangeRule(5.5, 3754.2),
            RosterSegmentRequiredSegmentRule("roster segment needs a value"),
            RosterSegmentLabelLengthRule(16),
        ),
    )
}
