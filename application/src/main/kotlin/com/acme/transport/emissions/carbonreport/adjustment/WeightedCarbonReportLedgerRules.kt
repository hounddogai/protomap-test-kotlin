package com.acme.transport.emissions.carbonreport.adjustment

/** A rule a weighted carbon report ledger must pass. Check returns its problems, or none. */
interface WeightedCarbonReportLedgerRule {
    fun check(item: WeightedCarbonReportLedger): MutableList<String>
}

data class WeightedCarbonReportLedgerCodeLengthRule(var maxLength: Int) : WeightedCarbonReportLedgerRule {
    override fun check(item: WeightedCarbonReportLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportLedgerNotesLengthRule(var maxLength: Int) : WeightedCarbonReportLedgerRule {
    override fun check(item: WeightedCarbonReportLedger): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportLedgerBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : WeightedCarbonReportLedgerRule {
    override fun check(item: WeightedCarbonReportLedger): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class WeightedCarbonReportLedgerRequiredCodeRule(var message: String) : WeightedCarbonReportLedgerRule {
    override fun check(item: WeightedCarbonReportLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted carbon report ledger. */
data class WeightedCarbonReportLedgerValidator(var rules: MutableList<WeightedCarbonReportLedgerRule>) {
    fun validate(item: WeightedCarbonReportLedger): MutableList<String> {
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

    fun requireValid(item: WeightedCarbonReportLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCarbonReportLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCarbonReportLedgerValidator(): WeightedCarbonReportLedgerValidator {
    return WeightedCarbonReportLedgerValidator(
        mutableListOf<WeightedCarbonReportLedgerRule>(
            WeightedCarbonReportLedgerCodeLengthRule(120),
            WeightedCarbonReportLedgerNotesLengthRule(255),
            WeightedCarbonReportLedgerBatchSizeRangeRule(16, 64),
            WeightedCarbonReportLedgerRequiredCodeRule("weighted carbon report ledger needs this field"),
        ),
    )
}
