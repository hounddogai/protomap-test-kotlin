package com.acme.transport.freightpay.dispute.adjustment

/** A rule a dispute audit must pass. Check returns its problems, or none. */
interface DisputeAuditRule {
    fun check(item: DisputeAudit): MutableList<String>
}

data class DisputeAuditDiscountRangeRule(var min: Double, var max: Double) : DisputeAuditRule {
    override fun check(item: DisputeAudit): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class DisputeAuditSequenceRangeRule(var min: Int, var max: Int) : DisputeAuditRule {
    override fun check(item: DisputeAudit): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAuditCodePrefixRule(var prefix: String) : DisputeAuditRule {
    override fun check(item: DisputeAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAuditMinimumRangeRule(var min: Double, var max: Double) : DisputeAuditRule {
    override fun check(item: DisputeAudit): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute audit. */
data class DisputeAuditValidator(var rules: MutableList<DisputeAuditRule>) {
    fun validate(item: DisputeAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeAuditValidator(): DisputeAuditValidator {
    return DisputeAuditValidator(
        mutableListOf<DisputeAuditRule>(
            DisputeAuditDiscountRangeRule(3.1, 1956.1),
            DisputeAuditSequenceRangeRule(255, 86400),
            DisputeAuditCodePrefixRule("Q"),
            DisputeAuditMinimumRangeRule(5.0, 1392.3),
        ),
    )
}
