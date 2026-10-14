package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.text.normalize

/** A rule a default relay schedule must pass. Check returns its problems, or none. */
interface DefaultRelayScheduleRule {
    fun check(item: DefaultRelaySchedule): MutableList<String>
}

data class DefaultRelayScheduleRequiredTitleRule(var message: String) : DefaultRelayScheduleRule {
    override fun check(item: DefaultRelaySchedule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayScheduleRequiredUnitRule(var message: String) : DefaultRelayScheduleRule {
    override fun check(item: DefaultRelaySchedule): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayScheduleLoadKgRangeRule(var min: Double, var max: Double) : DefaultRelayScheduleRule {
    override fun check(item: DefaultRelaySchedule): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class DefaultRelayScheduleAllowedCodeRule(var allowed: MutableList<String>) : DefaultRelayScheduleRule {
    override fun check(item: DefaultRelaySchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayScheduleTitleLengthRule(var maxLength: Int) : DefaultRelayScheduleRule {
    override fun check(item: DefaultRelaySchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayScheduleRequiredCodeRule(var message: String) : DefaultRelayScheduleRule {
    override fun check(item: DefaultRelaySchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default relay schedule. */
data class DefaultRelayScheduleValidator(var rules: MutableList<DefaultRelayScheduleRule>) {
    fun validate(item: DefaultRelaySchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRelaySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRelaySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRelayScheduleValidator(): DefaultRelayScheduleValidator {
    return DefaultRelayScheduleValidator(
        mutableListOf<DefaultRelayScheduleRule>(
            DefaultRelayScheduleRequiredTitleRule("default relay schedule needs a value"),
            DefaultRelayScheduleRequiredUnitRule("default relay schedule needs an entry"),
            DefaultRelayScheduleLoadKgRangeRule(3.5, 243.9),
            DefaultRelayScheduleAllowedCodeRule(mutableListOf<String>("capped", "capped")),
            DefaultRelayScheduleTitleLengthRule(86400),
            DefaultRelayScheduleRequiredCodeRule("default relay schedule needs this field"),
        ),
    )
}
