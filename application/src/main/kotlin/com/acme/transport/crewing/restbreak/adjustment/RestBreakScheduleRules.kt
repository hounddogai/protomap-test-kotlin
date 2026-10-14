package com.acme.transport.crewing.restbreak.adjustment

/** A rule a rest break schedule must pass. Check returns its problems, or none. */
interface RestBreakScheduleRule {
    fun check(item: RestBreakSchedule): MutableList<String>
}

data class RestBreakScheduleRequiredDescriptionRule(var message: String) : RestBreakScheduleRule {
    override fun check(item: RestBreakSchedule): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakScheduleRequiredCodeRule(var message: String) : RestBreakScheduleRule {
    override fun check(item: RestBreakSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakScheduleWeightRangeRule(var min: Double, var max: Double) : RestBreakScheduleRule {
    override fun check(item: RestBreakSchedule): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakScheduleCodePrefixRule(var prefix: String) : RestBreakScheduleRule {
    override fun check(item: RestBreakSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakScheduleCodeLengthRule(var maxLength: Int) : RestBreakScheduleRule {
    override fun check(item: RestBreakSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakScheduleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RestBreakScheduleRule {
    override fun check(item: RestBreakSchedule): MutableList<String> {
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

/** Runs every rule of a rest break schedule. */
data class RestBreakScheduleValidator(var rules: MutableList<RestBreakScheduleRule>) {
    fun validate(item: RestBreakSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakScheduleValidator(): RestBreakScheduleValidator {
    return RestBreakScheduleValidator(
        mutableListOf<RestBreakScheduleRule>(
            RestBreakScheduleRequiredDescriptionRule("rest break schedule needs an entry"),
            RestBreakScheduleRequiredCodeRule("rest break schedule needs this field"),
            RestBreakScheduleWeightRangeRule(7.4, 1258.9),
            RestBreakScheduleCodePrefixRule("OPS"),
            RestBreakScheduleCodeLengthRule(16),
            RestBreakScheduleTagsLimitRule(120, mutableListOf<String>("adaptive", "primary")),
        ),
    )
}
