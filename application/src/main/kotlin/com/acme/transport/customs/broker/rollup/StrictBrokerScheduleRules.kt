package com.acme.transport.customs.broker.rollup

/** A rule a strict broker schedule must pass. Check returns its problems, or none. */
interface StrictBrokerScheduleRule {
    fun check(item: StrictBrokerSchedule): MutableList<String>
}

data class StrictBrokerScheduleRequiredCategoryRule(var message: String) : StrictBrokerScheduleRule {
    override fun check(item: StrictBrokerSchedule): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerScheduleExpiresAtRangeRule(var min: Int, var max: Int) : StrictBrokerScheduleRule {
    override fun check(item: StrictBrokerSchedule): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerScheduleRequiredCodeRule(var message: String) : StrictBrokerScheduleRule {
    override fun check(item: StrictBrokerSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerScheduleRequiredNameRule(var message: String) : StrictBrokerScheduleRule {
    override fun check(item: StrictBrokerSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict broker schedule. */
data class StrictBrokerScheduleValidator(var rules: MutableList<StrictBrokerScheduleRule>) {
    fun validate(item: StrictBrokerSchedule): MutableList<String> {
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

    fun requireValid(item: StrictBrokerSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictBrokerSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictBrokerScheduleValidator(): StrictBrokerScheduleValidator {
    return StrictBrokerScheduleValidator(
        mutableListOf<StrictBrokerScheduleRule>(
            StrictBrokerScheduleRequiredCategoryRule("strict broker schedule needs an entry"),
            StrictBrokerScheduleExpiresAtRangeRule(16, 255),
            StrictBrokerScheduleRequiredCodeRule("strict broker schedule needs this field"),
            StrictBrokerScheduleRequiredNameRule("strict broker schedule needs an entry"),
        ),
    )
}
