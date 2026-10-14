package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.text.normalize

/** A rule a manual trailer yard schedule must pass. Check returns its problems, or none. */
interface ManualTrailerYardScheduleRule {
    fun check(item: ManualTrailerYardSchedule): MutableList<String>
}

data class ManualTrailerYardScheduleRetriesRangeRule(
    var min: Int,
    var max: Int,
) : ManualTrailerYardScheduleRule {
    override fun check(item: ManualTrailerYardSchedule): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class ManualTrailerYardScheduleRequiredCodeRule(var message: String) : ManualTrailerYardScheduleRule {
    override fun check(item: ManualTrailerYardSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardScheduleAllowedLabelRule(
    var allowed: MutableList<String>,
) : ManualTrailerYardScheduleRule {
    override fun check(item: ManualTrailerYardSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardScheduleWeightRangeRule(
    var min: Double,
    var max: Double,
) : ManualTrailerYardScheduleRule {
    override fun check(item: ManualTrailerYardSchedule): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardScheduleCodePrefixRule(var prefix: String) : ManualTrailerYardScheduleRule {
    override fun check(item: ManualTrailerYardSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer yard schedule. */
data class ManualTrailerYardScheduleValidator(var rules: MutableList<ManualTrailerYardScheduleRule>) {
    fun validate(item: ManualTrailerYardSchedule): MutableList<String> {
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

    fun requireValid(item: ManualTrailerYardSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerYardSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerYardScheduleValidator(): ManualTrailerYardScheduleValidator {
    return ManualTrailerYardScheduleValidator(
        mutableListOf<ManualTrailerYardScheduleRule>(
            ManualTrailerYardScheduleRetriesRangeRule(16, 1000),
            ManualTrailerYardScheduleRequiredCodeRule("manual trailer yard schedule needs this field"),
            ManualTrailerYardScheduleAllowedLabelRule(mutableListOf<String>("pending")),
            ManualTrailerYardScheduleWeightRangeRule(2.3, 4305.8),
            ManualTrailerYardScheduleCodePrefixRule("Q"),
        ),
    )
}
