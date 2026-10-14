package com.acme.transport.customs.hold.mapping

/** A rule a hold segment must pass. Check returns its problems, or none. */
interface HoldSegmentRule {
    fun check(item: HoldSegment): MutableList<String>
}

data class HoldSegmentRequiredTitleRule(var message: String) : HoldSegmentRule {
    override fun check(item: HoldSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldSegmentRequiredCodeRule(var message: String) : HoldSegmentRule {
    override fun check(item: HoldSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldSegmentRateRangeRule(var min: Double, var max: Double) : HoldSegmentRule {
    override fun check(item: HoldSegment): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold segment. */
data class HoldSegmentValidator(var rules: MutableList<HoldSegmentRule>) {
    fun validate(item: HoldSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldSegmentValidator(): HoldSegmentValidator {
    return HoldSegmentValidator(
        mutableListOf<HoldSegmentRule>(
            HoldSegmentRequiredTitleRule("hold segment needs a value"),
            HoldSegmentRequiredCodeRule("hold segment needs a value"),
            HoldSegmentRateRangeRule(6.6, 97.7),
        ),
    )
}
