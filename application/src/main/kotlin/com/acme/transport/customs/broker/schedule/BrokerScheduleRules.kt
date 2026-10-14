package com.acme.transport.customs.broker.schedule

/** A rule a broker schedule must pass. Check returns its problems, or none. */
interface BrokerScheduleRule {
    fun check(item: BrokerSchedule): MutableList<String>
}

data class BrokerScheduleRequiredTimezoneRule(var message: String) : BrokerScheduleRule {
    override fun check(item: BrokerSchedule): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerScheduleRequiredRegionRule(var message: String) : BrokerScheduleRule {
    override fun check(item: BrokerSchedule): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerScheduleGraceDaysRangeRule(var min: Int, var max: Int) : BrokerScheduleRule {
    override fun check(item: BrokerSchedule): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerScheduleRequiredTitleRule(var message: String) : BrokerScheduleRule {
    override fun check(item: BrokerSchedule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerScheduleCodePrefixRule(var prefix: String) : BrokerScheduleRule {
    override fun check(item: BrokerSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker schedule. */
data class BrokerScheduleValidator(var rules: MutableList<BrokerScheduleRule>) {
    fun validate(item: BrokerSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerScheduleValidator(): BrokerScheduleValidator {
    return BrokerScheduleValidator(
        mutableListOf<BrokerScheduleRule>(
            BrokerScheduleRequiredTimezoneRule("broker schedule needs a value"),
            BrokerScheduleRequiredRegionRule("broker schedule needs a value"),
            BrokerScheduleGraceDaysRangeRule(32, 64),
            BrokerScheduleRequiredTitleRule("broker schedule needs this field"),
            BrokerScheduleCodePrefixRule("X"),
        ),
    )
}
