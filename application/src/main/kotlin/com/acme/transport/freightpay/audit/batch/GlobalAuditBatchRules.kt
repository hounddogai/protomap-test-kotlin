package com.acme.transport.freightpay.audit.batch

/** A rule a global audit batch must pass. Check returns its problems, or none. */
interface GlobalAuditBatchRule {
    fun check(item: GlobalAuditBatch): MutableList<String>
}

data class GlobalAuditBatchRequiredReviewerRule(var message: String) : GlobalAuditBatchRule {
    override fun check(item: GlobalAuditBatch): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditBatchCapacityRangeRule(var min: Int, var max: Int) : GlobalAuditBatchRule {
    override fun check(item: GlobalAuditBatch): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class GlobalAuditBatchCodePrefixRule(var prefix: String) : GlobalAuditBatchRule {
    override fun check(item: GlobalAuditBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditBatchRequiredLabelRule(var message: String) : GlobalAuditBatchRule {
    override fun check(item: GlobalAuditBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global audit batch. */
data class GlobalAuditBatchValidator(var rules: MutableList<GlobalAuditBatchRule>) {
    fun validate(item: GlobalAuditBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalAuditBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAuditBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAuditBatchValidator(): GlobalAuditBatchValidator {
    return GlobalAuditBatchValidator(
        mutableListOf<GlobalAuditBatchRule>(
            GlobalAuditBatchRequiredReviewerRule("global audit batch needs this field"),
            GlobalAuditBatchCapacityRangeRule(80, 255),
            GlobalAuditBatchCodePrefixRule("REF"),
            GlobalAuditBatchRequiredLabelRule("global audit batch needs a value"),
        ),
    )
}
