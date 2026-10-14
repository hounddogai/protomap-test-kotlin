package com.acme.transport.emissions.idling.window

/** A rule a strict idling segment must pass. Check returns its problems, or none. */
interface StrictIdlingSegmentRule {
    fun check(item: StrictIdlingSegment): MutableList<String>
}

data class StrictIdlingSegmentRequiredTitleRule(var message: String) : StrictIdlingSegmentRule {
    override fun check(item: StrictIdlingSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIdlingSegmentTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : StrictIdlingSegmentRule {
    override fun check(item: StrictIdlingSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class StrictIdlingSegmentCapacityRangeRule(var min: Int, var max: Int) : StrictIdlingSegmentRule {
    override fun check(item: StrictIdlingSegment): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictIdlingSegmentRequiredCurrencyRule(var message: String) : StrictIdlingSegmentRule {
    override fun check(item: StrictIdlingSegment): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIdlingSegmentTitleLengthRule(var maxLength: Int) : StrictIdlingSegmentRule {
    override fun check(item: StrictIdlingSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict idling segment. */
data class StrictIdlingSegmentValidator(var rules: MutableList<StrictIdlingSegmentRule>) {
    fun validate(item: StrictIdlingSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictIdlingSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictIdlingSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictIdlingSegmentValidator(): StrictIdlingSegmentValidator {
    return StrictIdlingSegmentValidator(
        mutableListOf<StrictIdlingSegmentRule>(
            StrictIdlingSegmentRequiredTitleRule("strict idling segment needs this field"),
            StrictIdlingSegmentTagsLimitRule(120, mutableListOf<String>("global", "global", "global")),
            StrictIdlingSegmentCapacityRangeRule(16, 86400),
            StrictIdlingSegmentRequiredCurrencyRule("strict idling segment needs a value"),
            StrictIdlingSegmentTitleLengthRule(64),
        ),
    )
}
