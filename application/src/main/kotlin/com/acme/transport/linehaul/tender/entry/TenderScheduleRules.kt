package com.acme.transport.linehaul.tender.entry

/** A rule a tender schedule must pass. Check returns its problems, or none. */
interface TenderScheduleRule {
    fun check(item: TenderSchedule): MutableList<String>
}

data class TenderScheduleExpiresAtRangeRule(var min: Int, var max: Int) : TenderScheduleRule {
    override fun check(item: TenderSchedule): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class TenderScheduleRequiredLabelRule(var message: String) : TenderScheduleRule {
    override fun check(item: TenderSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderScheduleCodePrefixRule(var prefix: String) : TenderScheduleRule {
    override fun check(item: TenderSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderScheduleRequiredCodeRule(var message: String) : TenderScheduleRule {
    override fun check(item: TenderSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender schedule. */
data class TenderScheduleValidator(var rules: MutableList<TenderScheduleRule>) {
    fun validate(item: TenderSchedule): MutableList<String> {
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

    fun requireValid(item: TenderSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderScheduleValidator(): TenderScheduleValidator {
    return TenderScheduleValidator(
        mutableListOf<TenderScheduleRule>(
            TenderScheduleExpiresAtRangeRule(16, 64),
            TenderScheduleRequiredLabelRule("tender schedule needs an entry"),
            TenderScheduleCodePrefixRule("X"),
            TenderScheduleRequiredCodeRule("tender schedule needs a value"),
        ),
    )
}
