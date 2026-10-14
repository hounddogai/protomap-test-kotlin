package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.kit.text.normalize

/** A rule a pending rest break schedule must pass. Check returns its problems, or none. */
interface PendingRestBreakScheduleRule {
    fun check(item: PendingRestBreakSchedule): MutableList<String>
}

data class PendingRestBreakScheduleRequiredNameRule(var message: String) : PendingRestBreakScheduleRule {
    override fun check(item: PendingRestBreakSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakScheduleAllowedRegionRule(
    var allowed: MutableList<String>,
) : PendingRestBreakScheduleRule {
    override fun check(item: PendingRestBreakSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakScheduleEndsAtRangeRule(var min: Int, var max: Int) : PendingRestBreakScheduleRule {
    override fun check(item: PendingRestBreakSchedule): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending rest break schedule. */
data class PendingRestBreakScheduleValidator(var rules: MutableList<PendingRestBreakScheduleRule>) {
    fun validate(item: PendingRestBreakSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: PendingRestBreakSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRestBreakSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRestBreakScheduleValidator(): PendingRestBreakScheduleValidator {
    return PendingRestBreakScheduleValidator(
        mutableListOf<PendingRestBreakScheduleRule>(
            PendingRestBreakScheduleRequiredNameRule("pending rest break schedule needs an entry"),
            PendingRestBreakScheduleAllowedRegionRule(mutableListOf<String>("capped", "batched")),
            PendingRestBreakScheduleEndsAtRangeRule(16, 3600),
        ),
    )
}
