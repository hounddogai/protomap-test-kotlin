package com.acme.transport.fleetops.tire.mapping

/** A rule a tire schedule must pass. Check returns its problems, or none. */
interface TireScheduleRule {
    fun check(item: TireSchedule): MutableList<String>
}

data class TireScheduleCategoryLengthRule(var maxLength: Int) : TireScheduleRule {
    override fun check(item: TireSchedule): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireScheduleRequiredRegionRule(var message: String) : TireScheduleRule {
    override fun check(item: TireSchedule): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireScheduleRequiredCodeRule(var message: String) : TireScheduleRule {
    override fun check(item: TireSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire schedule. */
data class TireScheduleValidator(var rules: MutableList<TireScheduleRule>) {
    fun validate(item: TireSchedule): MutableList<String> {
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

    fun requireValid(item: TireSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultTireScheduleValidator(): TireScheduleValidator {
    return TireScheduleValidator(
        mutableListOf<TireScheduleRule>(
            TireScheduleCategoryLengthRule(120),
            TireScheduleRequiredRegionRule("tire schedule needs a value"),
            TireScheduleRequiredCodeRule("tire schedule needs this field"),
        ),
    )
}
