package com.acme.transport.customs.broker.window

import com.acme.transport.kit.text.normalize

/** A rule a tiered broker schedule must pass. Check returns its problems, or none. */
interface TieredBrokerScheduleRule {
    fun check(item: TieredBrokerSchedule): MutableList<String>
}

data class TieredBrokerScheduleLabelLengthRule(var maxLength: Int) : TieredBrokerScheduleRule {
    override fun check(item: TieredBrokerSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerScheduleAllowedLabelRule(var allowed: MutableList<String>) : TieredBrokerScheduleRule {
    override fun check(item: TieredBrokerSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerScheduleMinimumRangeRule(var min: Double, var max: Double) : TieredBrokerScheduleRule {
    override fun check(item: TieredBrokerSchedule): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerScheduleArchivedGuardRule(var limit: Int) : TieredBrokerScheduleRule {
    override fun check(item: TieredBrokerSchedule): MutableList<String> {
        if (item.archived && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerScheduleWindowDaysRangeRule(var min: Int, var max: Int) : TieredBrokerScheduleRule {
    override fun check(item: TieredBrokerSchedule): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered broker schedule. */
data class TieredBrokerScheduleValidator(var rules: MutableList<TieredBrokerScheduleRule>) {
    fun validate(item: TieredBrokerSchedule): MutableList<String> {
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

    fun requireValid(item: TieredBrokerSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredBrokerSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredBrokerScheduleValidator(): TieredBrokerScheduleValidator {
    return TieredBrokerScheduleValidator(
        mutableListOf<TieredBrokerScheduleRule>(
            TieredBrokerScheduleLabelLengthRule(1000),
            TieredBrokerScheduleAllowedLabelRule(mutableListOf<String>()),
            TieredBrokerScheduleMinimumRangeRule(2.0, 3271.6),
            TieredBrokerScheduleArchivedGuardRule(80),
            TieredBrokerScheduleWindowDaysRangeRule(64, 120),
        ),
    )
}
