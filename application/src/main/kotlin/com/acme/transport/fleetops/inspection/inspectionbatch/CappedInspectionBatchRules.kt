package com.acme.transport.fleetops.inspection.inspectionbatch

/** A rule a capped inspection batch must pass. Check returns its problems, or none. */
interface CappedInspectionBatchRule {
    fun check(item: CappedInspectionBatch): MutableList<String>
}

data class CappedInspectionBatchCodeLengthRule(var maxLength: Int) : CappedInspectionBatchRule {
    override fun check(item: CappedInspectionBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionBatchRequiredCodeRule(var message: String) : CappedInspectionBatchRule {
    override fun check(item: CappedInspectionBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionBatchScoreRangeRule(var min: Double, var max: Double) : CappedInspectionBatchRule {
    override fun check(item: CappedInspectionBatch): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped inspection batch. */
data class CappedInspectionBatchValidator(var rules: MutableList<CappedInspectionBatchRule>) {
    fun validate(item: CappedInspectionBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedInspectionBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedInspectionBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedInspectionBatchValidator(): CappedInspectionBatchValidator {
    return CappedInspectionBatchValidator(
        mutableListOf<CappedInspectionBatchRule>(
            CappedInspectionBatchCodeLengthRule(86400),
            CappedInspectionBatchRequiredCodeRule("capped inspection batch needs an entry"),
            CappedInspectionBatchScoreRangeRule(1.6, 3673.1),
        ),
    )
}
