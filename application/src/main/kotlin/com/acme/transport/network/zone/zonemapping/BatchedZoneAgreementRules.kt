package com.acme.transport.network.zone.zonemapping

/** A rule a batched zone agreement must pass. Check returns its problems, or none. */
interface BatchedZoneAgreementRule {
    fun check(item: BatchedZoneAgreement): MutableList<String>
}

data class BatchedZoneAgreementScoreRangeRule(var min: Double, var max: Double) : BatchedZoneAgreementRule {
    override fun check(item: BatchedZoneAgreement): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneAgreementRetriesRangeRule(var min: Int, var max: Int) : BatchedZoneAgreementRule {
    override fun check(item: BatchedZoneAgreement): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedZoneAgreementLabelLengthRule(var maxLength: Int) : BatchedZoneAgreementRule {
    override fun check(item: BatchedZoneAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched zone agreement. */
data class BatchedZoneAgreementValidator(var rules: MutableList<BatchedZoneAgreementRule>) {
    fun validate(item: BatchedZoneAgreement): MutableList<String> {
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

    fun requireValid(item: BatchedZoneAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedZoneAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedZoneAgreementValidator(): BatchedZoneAgreementValidator {
    return BatchedZoneAgreementValidator(
        mutableListOf<BatchedZoneAgreementRule>(
            BatchedZoneAgreementScoreRangeRule(7.8, 988.1),
            BatchedZoneAgreementRetriesRangeRule(32, 80),
            BatchedZoneAgreementLabelLengthRule(86400),
        ),
    )
}
