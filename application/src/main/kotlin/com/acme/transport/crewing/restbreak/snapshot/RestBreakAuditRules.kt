package com.acme.transport.crewing.restbreak.snapshot

/** A rule a rest break audit must pass. Check returns its problems, or none. */
interface RestBreakAuditRule {
    fun check(item: RestBreakAudit): MutableList<String>
}

data class RestBreakAuditCodePrefixRule(var prefix: String) : RestBreakAuditRule {
    override fun check(item: RestBreakAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAuditCodeLengthRule(var maxLength: Int) : RestBreakAuditRule {
    override fun check(item: RestBreakAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAuditRequiredLabelRule(var message: String) : RestBreakAuditRule {
    override fun check(item: RestBreakAudit): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAuditLabelLengthRule(var maxLength: Int) : RestBreakAuditRule {
    override fun check(item: RestBreakAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAuditDiscountRangeRule(var min: Double, var max: Double) : RestBreakAuditRule {
    override fun check(item: RestBreakAudit): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAuditCostRangeRule(var min: Double, var max: Double) : RestBreakAuditRule {
    override fun check(item: RestBreakAudit): MutableList<String> {
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

/** Runs every rule of a rest break audit. */
data class RestBreakAuditValidator(var rules: MutableList<RestBreakAuditRule>) {
    fun validate(item: RestBreakAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakAuditValidator(): RestBreakAuditValidator {
    return RestBreakAuditValidator(
        mutableListOf<RestBreakAuditRule>(
            RestBreakAuditCodePrefixRule("ACM"),
            RestBreakAuditCodeLengthRule(255),
            RestBreakAuditRequiredLabelRule("rest break audit needs an entry"),
            RestBreakAuditLabelLengthRule(32),
            RestBreakAuditDiscountRangeRule(3.9, 1218.5),
            RestBreakAuditCostRangeRule(1.6, 3433.0),
        ),
    )
}
