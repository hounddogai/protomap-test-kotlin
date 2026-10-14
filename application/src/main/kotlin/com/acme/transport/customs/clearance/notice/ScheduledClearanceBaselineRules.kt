package com.acme.transport.customs.clearance.notice

/** A rule a scheduled clearance baseline must pass. Check returns its problems, or none. */
interface ScheduledClearanceBaselineRule {
    fun check(item: ScheduledClearanceBaseline): MutableList<String>
}

data class ScheduledClearanceBaselineRequiredCodeRule(var message: String) : ScheduledClearanceBaselineRule {
    override fun check(item: ScheduledClearanceBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledClearanceBaselineRequiredSummaryRule(var message: String) : ScheduledClearanceBaselineRule {
    override fun check(item: ScheduledClearanceBaseline): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledClearanceBaselineRevisionRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledClearanceBaselineRule {
    override fun check(item: ScheduledClearanceBaseline): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class ScheduledClearanceBaselineCodeLengthRule(var maxLength: Int) : ScheduledClearanceBaselineRule {
    override fun check(item: ScheduledClearanceBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledClearanceBaselineGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledClearanceBaselineRule {
    override fun check(item: ScheduledClearanceBaseline): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled clearance baseline. */
data class ScheduledClearanceBaselineValidator(var rules: MutableList<ScheduledClearanceBaselineRule>) {
    fun validate(item: ScheduledClearanceBaseline): MutableList<String> {
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

    fun requireValid(item: ScheduledClearanceBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledClearanceBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledClearanceBaselineValidator(): ScheduledClearanceBaselineValidator {
    return ScheduledClearanceBaselineValidator(
        mutableListOf<ScheduledClearanceBaselineRule>(
            ScheduledClearanceBaselineRequiredCodeRule("scheduled clearance baseline needs a value"),
            ScheduledClearanceBaselineRequiredSummaryRule("scheduled clearance baseline needs a value"),
            ScheduledClearanceBaselineRevisionRangeRule(16, 3600),
            ScheduledClearanceBaselineCodeLengthRule(255),
            ScheduledClearanceBaselineGraceDaysRangeRule(80, 120),
        ),
    )
}
