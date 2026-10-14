package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.text.normalize

/** A rule a global inspection schedule must pass. Check returns its problems, or none. */
interface GlobalInspectionScheduleRule {
    fun check(item: GlobalInspectionSchedule): MutableList<String>
}

data class GlobalInspectionScheduleRequiredNameRule(var message: String) : GlobalInspectionScheduleRule {
    override fun check(item: GlobalInspectionSchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionScheduleCurrencyLengthRule(var maxLength: Int) : GlobalInspectionScheduleRule {
    override fun check(item: GlobalInspectionSchedule): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionScheduleNotesLengthRule(var maxLength: Int) : GlobalInspectionScheduleRule {
    override fun check(item: GlobalInspectionSchedule): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionScheduleAllowedChannelRule(
    var allowed: MutableList<String>,
) : GlobalInspectionScheduleRule {
    override fun check(item: GlobalInspectionSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global inspection schedule. */
data class GlobalInspectionScheduleValidator(var rules: MutableList<GlobalInspectionScheduleRule>) {
    fun validate(item: GlobalInspectionSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalInspectionSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalInspectionSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalInspectionScheduleValidator(): GlobalInspectionScheduleValidator {
    return GlobalInspectionScheduleValidator(
        mutableListOf<GlobalInspectionScheduleRule>(
            GlobalInspectionScheduleRequiredNameRule("global inspection schedule needs a value"),
            GlobalInspectionScheduleCurrencyLengthRule(80),
            GlobalInspectionScheduleNotesLengthRule(86400),
            GlobalInspectionScheduleAllowedChannelRule(mutableListOf<String>("regional")),
        ),
    )
}
