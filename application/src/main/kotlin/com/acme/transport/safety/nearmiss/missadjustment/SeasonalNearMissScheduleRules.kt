package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.text.normalize

/** A rule a seasonal near miss schedule must pass. Check returns its problems, or none. */
interface SeasonalNearMissScheduleRule {
    fun check(item: SeasonalNearMissSchedule): MutableList<String>
}

data class SeasonalNearMissScheduleChecksumLengthRule(var maxLength: Int) : SeasonalNearMissScheduleRule {
    override fun check(item: SeasonalNearMissSchedule): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNearMissScheduleDepotLengthRule(var maxLength: Int) : SeasonalNearMissScheduleRule {
    override fun check(item: SeasonalNearMissSchedule): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNearMissScheduleRequiredDepotRule(var message: String) : SeasonalNearMissScheduleRule {
    override fun check(item: SeasonalNearMissSchedule): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNearMissScheduleAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : SeasonalNearMissScheduleRule {
    override fun check(item: SeasonalNearMissSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNearMissScheduleRequiredCodeRule(var message: String) : SeasonalNearMissScheduleRule {
    override fun check(item: SeasonalNearMissSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal near miss schedule. */
data class SeasonalNearMissScheduleValidator(var rules: MutableList<SeasonalNearMissScheduleRule>) {
    fun validate(item: SeasonalNearMissSchedule): MutableList<String> {
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

    fun requireValid(item: SeasonalNearMissSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalNearMissSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalNearMissScheduleValidator(): SeasonalNearMissScheduleValidator {
    return SeasonalNearMissScheduleValidator(
        mutableListOf<SeasonalNearMissScheduleRule>(
            SeasonalNearMissScheduleChecksumLengthRule(64),
            SeasonalNearMissScheduleDepotLengthRule(120),
            SeasonalNearMissScheduleRequiredDepotRule("seasonal near miss schedule needs this field"),
            SeasonalNearMissScheduleAllowedShiftCodeRule(mutableListOf<String>()),
            SeasonalNearMissScheduleRequiredCodeRule("seasonal near miss schedule needs this field"),
        ),
    )
}
