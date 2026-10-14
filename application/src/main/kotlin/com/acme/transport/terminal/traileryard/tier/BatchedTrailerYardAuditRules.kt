package com.acme.transport.terminal.traileryard.tier

/** A rule a batched trailer yard audit must pass. Check returns its problems, or none. */
interface BatchedTrailerYardAuditRule {
    fun check(item: BatchedTrailerYardAudit): MutableList<String>
}

data class BatchedTrailerYardAuditRequiredCountryRule(var message: String) : BatchedTrailerYardAuditRule {
    override fun check(item: BatchedTrailerYardAudit): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerYardAuditCodeLengthRule(var maxLength: Int) : BatchedTrailerYardAuditRule {
    override fun check(item: BatchedTrailerYardAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerYardAuditBatchSizeRangeRule(var min: Int, var max: Int) : BatchedTrailerYardAuditRule {
    override fun check(item: BatchedTrailerYardAudit): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer yard audit. */
data class BatchedTrailerYardAuditValidator(var rules: MutableList<BatchedTrailerYardAuditRule>) {
    fun validate(item: BatchedTrailerYardAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTrailerYardAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerYardAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerYardAuditValidator(): BatchedTrailerYardAuditValidator {
    return BatchedTrailerYardAuditValidator(
        mutableListOf<BatchedTrailerYardAuditRule>(
            BatchedTrailerYardAuditRequiredCountryRule("batched trailer yard audit needs this field"),
            BatchedTrailerYardAuditCodeLengthRule(64),
            BatchedTrailerYardAuditBatchSizeRangeRule(64, 1000),
        ),
    )
}
