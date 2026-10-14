package com.acme.transport.crewing.availability.segment

/** A rule a availability segment must pass. Check returns its problems, or none. */
interface AvailabilitySegmentRule {
    fun check(item: AvailabilitySegment): MutableList<String>
}

data class AvailabilitySegmentRequiredCodeRule(var message: String) : AvailabilitySegmentRule {
    override fun check(item: AvailabilitySegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySegmentRequiredNameRule(var message: String) : AvailabilitySegmentRule {
    override fun check(item: AvailabilitySegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilitySegmentRequiredSourceRule(var message: String) : AvailabilitySegmentRule {
    override fun check(item: AvailabilitySegment): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability segment. */
data class AvailabilitySegmentValidator(var rules: MutableList<AvailabilitySegmentRule>) {
    fun validate(item: AvailabilitySegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilitySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilitySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilitySegmentValidator(): AvailabilitySegmentValidator {
    return AvailabilitySegmentValidator(
        mutableListOf<AvailabilitySegmentRule>(
            AvailabilitySegmentRequiredCodeRule("availability segment needs a value"),
            AvailabilitySegmentRequiredNameRule("availability segment needs a value"),
            AvailabilitySegmentRequiredSourceRule("availability segment needs an entry"),
        ),
    )
}
