package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.text.normalize

/** A rule a batched idling variance must pass. Check returns its problems, or none. */
interface BatchedIdlingVarianceRule {
    fun check(item: BatchedIdlingVariance): MutableList<String>
}

data class BatchedIdlingVarianceRequiredCodeRule(var message: String) : BatchedIdlingVarianceRule {
    override fun check(item: BatchedIdlingVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingVarianceAllowedCodeRule(var allowed: MutableList<String>) : BatchedIdlingVarianceRule {
    override fun check(item: BatchedIdlingVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingVarianceCodeLengthRule(var maxLength: Int) : BatchedIdlingVarianceRule {
    override fun check(item: BatchedIdlingVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingVarianceSequenceRangeRule(var min: Int, var max: Int) : BatchedIdlingVarianceRule {
    override fun check(item: BatchedIdlingVariance): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched idling variance. */
data class BatchedIdlingVarianceValidator(var rules: MutableList<BatchedIdlingVarianceRule>) {
    fun validate(item: BatchedIdlingVariance): MutableList<String> {
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

    fun requireValid(item: BatchedIdlingVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedIdlingVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedIdlingVarianceValidator(): BatchedIdlingVarianceValidator {
    return BatchedIdlingVarianceValidator(
        mutableListOf<BatchedIdlingVarianceRule>(
            BatchedIdlingVarianceRequiredCodeRule("batched idling variance needs this field"),
            BatchedIdlingVarianceAllowedCodeRule(mutableListOf<String>("tiered")),
            BatchedIdlingVarianceCodeLengthRule(86400),
            BatchedIdlingVarianceSequenceRangeRule(32, 120),
        ),
    )
}
