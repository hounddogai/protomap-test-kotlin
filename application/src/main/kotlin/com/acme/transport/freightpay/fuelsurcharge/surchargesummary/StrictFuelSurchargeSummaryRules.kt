package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

/** A rule a strict fuel surcharge summary must pass. Check returns its problems, or none. */
interface StrictFuelSurchargeSummaryRule {
    fun check(item: StrictFuelSurchargeSummary): MutableList<String>
}

data class StrictFuelSurchargeSummaryArchivedGuardRule(var limit: Int) : StrictFuelSurchargeSummaryRule {
    override fun check(item: StrictFuelSurchargeSummary): MutableList<String> {
        if (item.archived && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelSurchargeSummaryRequiredCountryRule(var message: String) : StrictFuelSurchargeSummaryRule {
    override fun check(item: StrictFuelSurchargeSummary): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelSurchargeSummaryCodeLengthRule(var maxLength: Int) : StrictFuelSurchargeSummaryRule {
    override fun check(item: StrictFuelSurchargeSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict fuel surcharge summary. */
data class StrictFuelSurchargeSummaryValidator(var rules: MutableList<StrictFuelSurchargeSummaryRule>) {
    fun validate(item: StrictFuelSurchargeSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictFuelSurchargeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictFuelSurchargeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictFuelSurchargeSummaryValidator(): StrictFuelSurchargeSummaryValidator {
    return StrictFuelSurchargeSummaryValidator(
        mutableListOf<StrictFuelSurchargeSummaryRule>(
            StrictFuelSurchargeSummaryArchivedGuardRule(255),
            StrictFuelSurchargeSummaryRequiredCountryRule("strict fuel surcharge summary needs this field"),
            StrictFuelSurchargeSummaryCodeLengthRule(255),
        ),
    )
}
