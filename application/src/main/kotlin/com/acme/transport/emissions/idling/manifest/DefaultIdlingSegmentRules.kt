package com.acme.transport.emissions.idling.manifest

/** A rule a default idling segment must pass. Check returns its problems, or none. */
interface DefaultIdlingSegmentRule {
    fun check(item: DefaultIdlingSegment): MutableList<String>
}

data class DefaultIdlingSegmentTimezoneLengthRule(var maxLength: Int) : DefaultIdlingSegmentRule {
    override fun check(item: DefaultIdlingSegment): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingSegmentRequiredDockRule(var message: String) : DefaultIdlingSegmentRule {
    override fun check(item: DefaultIdlingSegment): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingSegmentWindowDaysRangeRule(var min: Int, var max: Int) : DefaultIdlingSegmentRule {
    override fun check(item: DefaultIdlingSegment): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class DefaultIdlingSegmentRequiredTimezoneRule(var message: String) : DefaultIdlingSegmentRule {
    override fun check(item: DefaultIdlingSegment): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default idling segment. */
data class DefaultIdlingSegmentValidator(var rules: MutableList<DefaultIdlingSegmentRule>) {
    fun validate(item: DefaultIdlingSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultIdlingSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultIdlingSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultIdlingSegmentValidator(): DefaultIdlingSegmentValidator {
    return DefaultIdlingSegmentValidator(
        mutableListOf<DefaultIdlingSegmentRule>(
            DefaultIdlingSegmentTimezoneLengthRule(255),
            DefaultIdlingSegmentRequiredDockRule("default idling segment needs this field"),
            DefaultIdlingSegmentWindowDaysRangeRule(64, 64),
            DefaultIdlingSegmentRequiredTimezoneRule("default idling segment needs a value"),
        ),
    )
}
