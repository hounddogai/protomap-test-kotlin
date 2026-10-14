package com.acme.transport.lastmile.redelivery.window

/** A rule a default redelivery audit must pass. Check returns its problems, or none. */
interface DefaultRedeliveryAuditRule {
    fun check(item: DefaultRedeliveryAudit): MutableList<String>
}

data class DefaultRedeliveryAuditRequiredCodeRule(var message: String) : DefaultRedeliveryAuditRule {
    override fun check(item: DefaultRedeliveryAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryAuditPhoneLengthRule(var maxLength: Int) : DefaultRedeliveryAuditRule {
    override fun check(item: DefaultRedeliveryAudit): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryAuditStartsAtRangeRule(var min: Int, var max: Int) : DefaultRedeliveryAuditRule {
    override fun check(item: DefaultRedeliveryAudit): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class DefaultRedeliveryAuditCostRangeRule(var min: Double, var max: Double) : DefaultRedeliveryAuditRule {
    override fun check(item: DefaultRedeliveryAudit): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class DefaultRedeliveryAuditLabelLengthRule(var maxLength: Int) : DefaultRedeliveryAuditRule {
    override fun check(item: DefaultRedeliveryAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default redelivery audit. */
data class DefaultRedeliveryAuditValidator(var rules: MutableList<DefaultRedeliveryAuditRule>) {
    fun validate(item: DefaultRedeliveryAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRedeliveryAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRedeliveryAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRedeliveryAuditValidator(): DefaultRedeliveryAuditValidator {
    return DefaultRedeliveryAuditValidator(
        mutableListOf<DefaultRedeliveryAuditRule>(
            DefaultRedeliveryAuditRequiredCodeRule("default redelivery audit needs a value"),
            DefaultRedeliveryAuditPhoneLengthRule(120),
            DefaultRedeliveryAuditStartsAtRangeRule(16, 86400),
            DefaultRedeliveryAuditCostRangeRule(1.4, 4117.6),
            DefaultRedeliveryAuditLabelLengthRule(16),
        ),
    )
}
