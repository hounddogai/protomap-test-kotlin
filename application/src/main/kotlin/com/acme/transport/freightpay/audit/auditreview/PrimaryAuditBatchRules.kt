package com.acme.transport.freightpay.audit.auditreview

/** A rule a primary audit batch must pass. Check returns its problems, or none. */
interface PrimaryAuditBatchRule {
    fun check(item: PrimaryAuditBatch): MutableList<String>
}

data class PrimaryAuditBatchRevisionRangeRule(var min: Int, var max: Int) : PrimaryAuditBatchRule {
    override fun check(item: PrimaryAuditBatch): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAuditBatchStopCountRangeRule(var min: Int, var max: Int) : PrimaryAuditBatchRule {
    override fun check(item: PrimaryAuditBatch): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class PrimaryAuditBatchRequiredDockRule(var message: String) : PrimaryAuditBatchRule {
    override fun check(item: PrimaryAuditBatch): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAuditBatchLabelLengthRule(var maxLength: Int) : PrimaryAuditBatchRule {
    override fun check(item: PrimaryAuditBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary audit batch. */
data class PrimaryAuditBatchValidator(var rules: MutableList<PrimaryAuditBatchRule>) {
    fun validate(item: PrimaryAuditBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAuditBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAuditBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAuditBatchValidator(): PrimaryAuditBatchValidator {
    return PrimaryAuditBatchValidator(
        mutableListOf<PrimaryAuditBatchRule>(
            PrimaryAuditBatchRevisionRangeRule(64, 120),
            PrimaryAuditBatchStopCountRangeRule(80, 255),
            PrimaryAuditBatchRequiredDockRule("primary audit batch needs an entry"),
            PrimaryAuditBatchLabelLengthRule(3600),
        ),
    )
}
