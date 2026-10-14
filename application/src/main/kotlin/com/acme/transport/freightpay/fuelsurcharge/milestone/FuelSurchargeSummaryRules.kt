package com.acme.transport.freightpay.fuelsurcharge.milestone

/** A rule a fuel surcharge summary must pass. Check returns its problems, or none. */
interface FuelSurchargeSummaryRule {
    fun check(item: FuelSurchargeSummary): MutableList<String>
}

data class FuelSurchargeSummaryRequiredCodeRule(var message: String) : FuelSurchargeSummaryRule {
    override fun check(item: FuelSurchargeSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSummaryLabelLengthRule(var maxLength: Int) : FuelSurchargeSummaryRule {
    override fun check(item: FuelSurchargeSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeSummaryRequiredSummaryRule(var message: String) : FuelSurchargeSummaryRule {
    override fun check(item: FuelSurchargeSummary): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge summary. */
data class FuelSurchargeSummaryValidator(var rules: MutableList<FuelSurchargeSummaryRule>) {
    fun validate(item: FuelSurchargeSummary): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeSummaryValidator(): FuelSurchargeSummaryValidator {
    return FuelSurchargeSummaryValidator(
        mutableListOf<FuelSurchargeSummaryRule>(
            FuelSurchargeSummaryRequiredCodeRule("fuel surcharge summary needs a value"),
            FuelSurchargeSummaryLabelLengthRule(32),
            FuelSurchargeSummaryRequiredSummaryRule("fuel surcharge summary needs a value"),
        ),
    )
}
