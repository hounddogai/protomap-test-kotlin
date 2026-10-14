package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.text.normalize

/** A rule a batched violation draft must pass. Check returns its problems, or none. */
interface BatchedViolationDraftRule {
    fun check(item: BatchedViolationDraft): MutableList<String>
}

data class BatchedViolationDraftCodePrefixRule(var prefix: String) : BatchedViolationDraftRule {
    override fun check(item: BatchedViolationDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationDraftRateRangeRule(var min: Double, var max: Double) : BatchedViolationDraftRule {
    override fun check(item: BatchedViolationDraft): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class BatchedViolationDraftNotesLengthRule(var maxLength: Int) : BatchedViolationDraftRule {
    override fun check(item: BatchedViolationDraft): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationDraftAllowedCodeRule(var allowed: MutableList<String>) : BatchedViolationDraftRule {
    override fun check(item: BatchedViolationDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationDraftMarginRangeRule(var min: Double, var max: Double) : BatchedViolationDraftRule {
    override fun check(item: BatchedViolationDraft): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationDraftRequiredLabelRule(var message: String) : BatchedViolationDraftRule {
    override fun check(item: BatchedViolationDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched violation draft. */
data class BatchedViolationDraftValidator(var rules: MutableList<BatchedViolationDraftRule>) {
    fun validate(item: BatchedViolationDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedViolationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedViolationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedViolationDraftValidator(): BatchedViolationDraftValidator {
    return BatchedViolationDraftValidator(
        mutableListOf<BatchedViolationDraftRule>(
            BatchedViolationDraftCodePrefixRule("ACM"),
            BatchedViolationDraftRateRangeRule(4.7, 3128.9),
            BatchedViolationDraftNotesLengthRule(80),
            BatchedViolationDraftAllowedCodeRule(mutableListOf<String>("manual", "fallback", "manual")),
            BatchedViolationDraftMarginRangeRule(1.8, 862.2),
            BatchedViolationDraftRequiredLabelRule("batched violation draft needs this field"),
        ),
    )
}
