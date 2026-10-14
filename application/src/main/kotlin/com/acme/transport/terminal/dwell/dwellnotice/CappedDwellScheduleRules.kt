package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.text.normalize

/** A rule a capped dwell schedule must pass. Check returns its problems, or none. */
interface CappedDwellScheduleRule {
    fun check(item: CappedDwellSchedule): MutableList<String>
}

data class CappedDwellScheduleStartsAtRangeRule(var min: Int, var max: Int) : CappedDwellScheduleRule {
    override fun check(item: CappedDwellSchedule): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellScheduleAllowedCodeRule(var allowed: MutableList<String>) : CappedDwellScheduleRule {
    override fun check(item: CappedDwellSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellScheduleRequiredCodeRule(var message: String) : CappedDwellScheduleRule {
    override fun check(item: CappedDwellSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellScheduleEtaMinutesRangeRule(var min: Int, var max: Int) : CappedDwellScheduleRule {
    override fun check(item: CappedDwellSchedule): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dwell schedule. */
data class CappedDwellScheduleValidator(var rules: MutableList<CappedDwellScheduleRule>) {
    fun validate(item: CappedDwellSchedule): MutableList<String> {
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

    fun requireValid(item: CappedDwellSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDwellSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDwellScheduleValidator(): CappedDwellScheduleValidator {
    return CappedDwellScheduleValidator(
        mutableListOf<CappedDwellScheduleRule>(
            CappedDwellScheduleStartsAtRangeRule(32, 32),
            CappedDwellScheduleAllowedCodeRule(mutableListOf<String>()),
            CappedDwellScheduleRequiredCodeRule("capped dwell schedule needs a value"),
            CappedDwellScheduleEtaMinutesRangeRule(32, 120),
        ),
    )
}
