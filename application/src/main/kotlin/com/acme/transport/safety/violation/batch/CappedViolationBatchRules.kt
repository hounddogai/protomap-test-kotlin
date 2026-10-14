package com.acme.transport.safety.violation.batch

/** A rule a capped violation batch must pass. Check returns its problems, or none. */
interface CappedViolationBatchRule {
    fun check(item: CappedViolationBatch): MutableList<String>
}

data class CappedViolationBatchCodePrefixRule(var prefix: String) : CappedViolationBatchRule {
    override fun check(item: CappedViolationBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationBatchLabelLengthRule(var maxLength: Int) : CappedViolationBatchRule {
    override fun check(item: CappedViolationBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationBatchRequiredCodeRule(var message: String) : CappedViolationBatchRule {
    override fun check(item: CappedViolationBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationBatchRequiredLabelRule(var message: String) : CappedViolationBatchRule {
    override fun check(item: CappedViolationBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped violation batch. */
data class CappedViolationBatchValidator(var rules: MutableList<CappedViolationBatchRule>) {
    fun validate(item: CappedViolationBatch): MutableList<String> {
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

    fun requireValid(item: CappedViolationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedViolationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedViolationBatchValidator(): CappedViolationBatchValidator {
    return CappedViolationBatchValidator(
        mutableListOf<CappedViolationBatchRule>(
            CappedViolationBatchCodePrefixRule("X"),
            CappedViolationBatchLabelLengthRule(86400),
            CappedViolationBatchRequiredCodeRule("capped violation batch needs an entry"),
            CappedViolationBatchRequiredLabelRule("capped violation batch needs this field"),
        ),
    )
}
