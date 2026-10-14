package com.acme.transport.fleetops.tire.segment

/** A rule a weighted tire segment must pass. Check returns its problems, or none. */
interface WeightedTireSegmentRule {
    fun check(item: WeightedTireSegment): MutableList<String>
}

data class WeightedTireSegmentRequiredNameRule(var message: String) : WeightedTireSegmentRule {
    override fun check(item: WeightedTireSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTireSegmentTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : WeightedTireSegmentRule {
    override fun check(item: WeightedTireSegment): MutableList<String> {
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

data class WeightedTireSegmentUpdatedAtRangeRule(var min: Int, var max: Int) : WeightedTireSegmentRule {
    override fun check(item: WeightedTireSegment): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class WeightedTireSegmentOdometerRangeRule(var min: Int, var max: Int) : WeightedTireSegmentRule {
    override fun check(item: WeightedTireSegment): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted tire segment. */
data class WeightedTireSegmentValidator(var rules: MutableList<WeightedTireSegmentRule>) {
    fun validate(item: WeightedTireSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTireSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTireSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTireSegmentValidator(): WeightedTireSegmentValidator {
    return WeightedTireSegmentValidator(
        mutableListOf<WeightedTireSegmentRule>(
            WeightedTireSegmentRequiredNameRule("weighted tire segment needs a value"),
            WeightedTireSegmentTagsLimitRule(80, mutableListOf<String>("strict", "adaptive", "default")),
            WeightedTireSegmentUpdatedAtRangeRule(1000, 86400),
            WeightedTireSegmentOdometerRangeRule(16, 1000),
        ),
    )
}
