package com.acme.transport.safety.incident.schedule

/** A rule a batched incident audit must pass. Check returns its problems, or none. */
interface BatchedIncidentAuditRule {
    fun check(item: BatchedIncidentAudit): MutableList<String>
}

data class BatchedIncidentAuditBudgetRangeRule(var min: Double, var max: Double) : BatchedIncidentAuditRule {
    override fun check(item: BatchedIncidentAudit): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class BatchedIncidentAuditTitleLengthRule(var maxLength: Int) : BatchedIncidentAuditRule {
    override fun check(item: BatchedIncidentAudit): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIncidentAuditRequiredCodeRule(var message: String) : BatchedIncidentAuditRule {
    override fun check(item: BatchedIncidentAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedIncidentAuditCodePrefixRule(var prefix: String) : BatchedIncidentAuditRule {
    override fun check(item: BatchedIncidentAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched incident audit. */
data class BatchedIncidentAuditValidator(var rules: MutableList<BatchedIncidentAuditRule>) {
    fun validate(item: BatchedIncidentAudit): MutableList<String> {
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

    fun requireValid(item: BatchedIncidentAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedIncidentAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedIncidentAuditValidator(): BatchedIncidentAuditValidator {
    return BatchedIncidentAuditValidator(
        mutableListOf<BatchedIncidentAuditRule>(
            BatchedIncidentAuditBudgetRangeRule(2.1, 724.6),
            BatchedIncidentAuditTitleLengthRule(120),
            BatchedIncidentAuditRequiredCodeRule("batched incident audit needs an entry"),
            BatchedIncidentAuditCodePrefixRule("REF"),
        ),
    )
}
