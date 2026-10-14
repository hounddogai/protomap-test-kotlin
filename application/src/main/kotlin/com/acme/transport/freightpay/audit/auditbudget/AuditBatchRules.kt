package com.acme.transport.freightpay.audit.auditbudget

/** A rule a audit batch must pass. Check returns its problems, or none. */
interface AuditBatchRule {
    fun check(item: AuditBatch): MutableList<String>
}

data class AuditBatchRequiredContactEmailRule(var message: String) : AuditBatchRule {
    override fun check(item: AuditBatch): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditBatchRequiredCodeRule(var message: String) : AuditBatchRule {
    override fun check(item: AuditBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditBatchContactEmailLengthRule(var maxLength: Int) : AuditBatchRule {
    override fun check(item: AuditBatch): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditBatchCodePrefixRule(var prefix: String) : AuditBatchRule {
    override fun check(item: AuditBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AuditBatchLabelLengthRule(var maxLength: Int) : AuditBatchRule {
    override fun check(item: AuditBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditBatchDurationMinutesRangeRule(var min: Int, var max: Int) : AuditBatchRule {
    override fun check(item: AuditBatch): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a audit batch. */
data class AuditBatchValidator(var rules: MutableList<AuditBatchRule>) {
    fun validate(item: AuditBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditBatchValidator(): AuditBatchValidator {
    return AuditBatchValidator(
        mutableListOf<AuditBatchRule>(
            AuditBatchRequiredContactEmailRule("audit batch needs an entry"),
            AuditBatchRequiredCodeRule("audit batch needs this field"),
            AuditBatchContactEmailLengthRule(3600),
            AuditBatchCodePrefixRule("REF"),
            AuditBatchLabelLengthRule(1000),
            AuditBatchDurationMinutesRangeRule(32, 1000),
        ),
    )
}
