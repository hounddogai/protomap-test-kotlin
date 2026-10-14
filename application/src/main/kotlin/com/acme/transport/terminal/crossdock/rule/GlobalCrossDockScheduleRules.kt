package com.acme.transport.terminal.crossdock.rule

/** A rule a global cross dock schedule must pass. Check returns its problems, or none. */
interface GlobalCrossDockScheduleRule {
    fun check(item: GlobalCrossDockSchedule): MutableList<String>
}

data class GlobalCrossDockScheduleRequiredNameRule(var message: String) : GlobalCrossDockScheduleRule {
    override fun check(item: GlobalCrossDockSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCrossDockScheduleAmountRangeRule(
    var min: Double,
    var max: Double,
) : GlobalCrossDockScheduleRule {
    override fun check(item: GlobalCrossDockSchedule): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class GlobalCrossDockScheduleRequiredCodeRule(var message: String) : GlobalCrossDockScheduleRule {
    override fun check(item: GlobalCrossDockSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global cross dock schedule. */
data class GlobalCrossDockScheduleValidator(var rules: MutableList<GlobalCrossDockScheduleRule>) {
    fun validate(item: GlobalCrossDockSchedule): MutableList<String> {
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

    fun requireValid(item: GlobalCrossDockSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCrossDockSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCrossDockScheduleValidator(): GlobalCrossDockScheduleValidator {
    return GlobalCrossDockScheduleValidator(
        mutableListOf<GlobalCrossDockScheduleRule>(
            GlobalCrossDockScheduleRequiredNameRule("global cross dock schedule needs this field"),
            GlobalCrossDockScheduleAmountRangeRule(9.5, 1354.9),
            GlobalCrossDockScheduleRequiredCodeRule("global cross dock schedule needs this field"),
        ),
    )
}
