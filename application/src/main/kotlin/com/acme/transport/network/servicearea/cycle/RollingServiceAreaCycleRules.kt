package com.acme.transport.network.servicearea.cycle

/** A rule a rolling service area cycle must pass. Check returns its problems, or none. */
interface RollingServiceAreaCycleRule {
    fun check(item: RollingServiceAreaCycle): MutableList<String>
}

data class RollingServiceAreaCycleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingServiceAreaCycleRule {
    override fun check(item: RollingServiceAreaCycle): MutableList<String> {
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

data class RollingServiceAreaCycleRequiredDescriptionRule(var message: String) : RollingServiceAreaCycleRule {
    override fun check(item: RollingServiceAreaCycle): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingServiceAreaCycleCodePrefixRule(var prefix: String) : RollingServiceAreaCycleRule {
    override fun check(item: RollingServiceAreaCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingServiceAreaCycleOdometerRangeRule(var min: Int, var max: Int) : RollingServiceAreaCycleRule {
    override fun check(item: RollingServiceAreaCycle): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling service area cycle. */
data class RollingServiceAreaCycleValidator(var rules: MutableList<RollingServiceAreaCycleRule>) {
    fun validate(item: RollingServiceAreaCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingServiceAreaCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingServiceAreaCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingServiceAreaCycleValidator(): RollingServiceAreaCycleValidator {
    return RollingServiceAreaCycleValidator(
        mutableListOf<RollingServiceAreaCycleRule>(
            RollingServiceAreaCycleTagsLimitRule(16, mutableListOf<String>("primary", "default", "rolling")),
            RollingServiceAreaCycleRequiredDescriptionRule("rolling service area cycle needs an entry"),
            RollingServiceAreaCycleCodePrefixRule("Q"),
            RollingServiceAreaCycleOdometerRangeRule(16, 32),
        ),
    )
}
