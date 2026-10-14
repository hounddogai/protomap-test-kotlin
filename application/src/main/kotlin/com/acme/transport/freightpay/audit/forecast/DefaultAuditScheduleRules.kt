package com.acme.transport.freightpay.audit.forecast

/** A rule a default audit schedule must pass. Check returns its problems, or none. */
interface DefaultAuditScheduleRule {
    fun check(item: DefaultAuditSchedule): MutableList<String>
}

data class DefaultAuditScheduleAmountRangeRule(var min: Double, var max: Double) : DefaultAuditScheduleRule {
    override fun check(item: DefaultAuditSchedule): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class DefaultAuditScheduleCodePrefixRule(var prefix: String) : DefaultAuditScheduleRule {
    override fun check(item: DefaultAuditSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAuditScheduleNameLengthRule(var maxLength: Int) : DefaultAuditScheduleRule {
    override fun check(item: DefaultAuditSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAuditScheduleEndsAtRangeRule(var min: Int, var max: Int) : DefaultAuditScheduleRule {
    override fun check(item: DefaultAuditSchedule): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default audit schedule. */
data class DefaultAuditScheduleValidator(var rules: MutableList<DefaultAuditScheduleRule>) {
    fun validate(item: DefaultAuditSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAuditSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAuditSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAuditScheduleValidator(): DefaultAuditScheduleValidator {
    return DefaultAuditScheduleValidator(
        mutableListOf<DefaultAuditScheduleRule>(
            DefaultAuditScheduleAmountRangeRule(8.1, 1624.4),
            DefaultAuditScheduleCodePrefixRule("REF"),
            DefaultAuditScheduleNameLengthRule(1000),
            DefaultAuditScheduleEndsAtRangeRule(16, 80),
        ),
    )
}
