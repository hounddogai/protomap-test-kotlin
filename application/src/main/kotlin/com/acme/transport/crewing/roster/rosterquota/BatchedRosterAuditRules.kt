package com.acme.transport.crewing.roster.rosterquota

/** A rule a batched roster audit must pass. Check returns its problems, or none. */
interface BatchedRosterAuditRule {
    fun check(item: BatchedRosterAudit): MutableList<String>
}

data class BatchedRosterAuditRatioRangeRule(var min: Double, var max: Double) : BatchedRosterAuditRule {
    override fun check(item: BatchedRosterAudit): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterAuditRequiredCurrencyRule(var message: String) : BatchedRosterAuditRule {
    override fun check(item: BatchedRosterAudit): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterAuditScoreRangeRule(var min: Double, var max: Double) : BatchedRosterAuditRule {
    override fun check(item: BatchedRosterAudit): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class BatchedRosterAuditCodePrefixRule(var prefix: String) : BatchedRosterAuditRule {
    override fun check(item: BatchedRosterAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched roster audit. */
data class BatchedRosterAuditValidator(var rules: MutableList<BatchedRosterAuditRule>) {
    fun validate(item: BatchedRosterAudit): MutableList<String> {
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

    fun requireValid(item: BatchedRosterAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRosterAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRosterAuditValidator(): BatchedRosterAuditValidator {
    return BatchedRosterAuditValidator(
        mutableListOf<BatchedRosterAuditRule>(
            BatchedRosterAuditRatioRangeRule(2.6, 126.9),
            BatchedRosterAuditRequiredCurrencyRule("batched roster audit needs an entry"),
            BatchedRosterAuditScoreRangeRule(1.0, 2304.6),
            BatchedRosterAuditCodePrefixRule("X"),
        ),
    )
}
