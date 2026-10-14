package com.acme.transport.terminal.crossdock.recorditem

/** A rule a cross dock schedule must pass. Check returns its problems, or none. */
interface CrossDockScheduleRule {
    fun check(item: CrossDockSchedule): MutableList<String>
}

data class CrossDockScheduleLoadKgRangeRule(var min: Double, var max: Double) : CrossDockScheduleRule {
    override fun check(item: CrossDockSchedule): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockScheduleRequiredCarrierRule(var message: String) : CrossDockScheduleRule {
    override fun check(item: CrossDockSchedule): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockScheduleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : CrossDockScheduleRule {
    override fun check(item: CrossDockSchedule): MutableList<String> {
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

data class CrossDockScheduleActiveGuardRule(var limit: Int) : CrossDockScheduleRule {
    override fun check(item: CrossDockSchedule): MutableList<String> {
        if (item.active && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockScheduleRequiredCodeRule(var message: String) : CrossDockScheduleRule {
    override fun check(item: CrossDockSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock schedule. */
data class CrossDockScheduleValidator(var rules: MutableList<CrossDockScheduleRule>) {
    fun validate(item: CrossDockSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockScheduleValidator(): CrossDockScheduleValidator {
    return CrossDockScheduleValidator(
        mutableListOf<CrossDockScheduleRule>(
            CrossDockScheduleLoadKgRangeRule(8.9, 2876.0),
            CrossDockScheduleRequiredCarrierRule("cross dock schedule needs this field"),
            CrossDockScheduleTagsLimitRule(16, mutableListOf<String>("seasonal")),
            CrossDockScheduleActiveGuardRule(3600),
            CrossDockScheduleRequiredCodeRule("cross dock schedule needs a value"),
        ),
    )
}
