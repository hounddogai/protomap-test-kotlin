package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.text.normalize

/** A rule a accessorial batch must pass. Check returns its problems, or none. */
interface AccessorialBatchRule {
    fun check(item: AccessorialBatch): MutableList<String>
}

data class AccessorialBatchAllowedLabelRule(var allowed: MutableList<String>) : AccessorialBatchRule {
    override fun check(item: AccessorialBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBatchRequiredLabelRule(var message: String) : AccessorialBatchRule {
    override fun check(item: AccessorialBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialBatchLabelLengthRule(var maxLength: Int) : AccessorialBatchRule {
    override fun check(item: AccessorialBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial batch. */
data class AccessorialBatchValidator(var rules: MutableList<AccessorialBatchRule>) {
    fun validate(item: AccessorialBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialBatchValidator(): AccessorialBatchValidator {
    return AccessorialBatchValidator(
        mutableListOf<AccessorialBatchRule>(
            AccessorialBatchAllowedLabelRule(mutableListOf<String>("weighted")),
            AccessorialBatchRequiredLabelRule("accessorial batch needs this field"),
            AccessorialBatchLabelLengthRule(16),
        ),
    )
}
