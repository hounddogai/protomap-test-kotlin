package com.acme.transport.lastmile.redelivery.redeliveryproposal

/** A rule a redelivery summary must pass. Check returns its problems, or none. */
interface RedeliverySummaryRule {
    fun check(item: RedeliverySummary): MutableList<String>
}

data class RedeliverySummaryRequiredCodeRule(var message: String) : RedeliverySummaryRule {
    override fun check(item: RedeliverySummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySummaryRequiredShiftCodeRule(var message: String) : RedeliverySummaryRule {
    override fun check(item: RedeliverySummary): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliverySummaryUnitLengthRule(var maxLength: Int) : RedeliverySummaryRule {
    override fun check(item: RedeliverySummary): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery summary. */
data class RedeliverySummaryValidator(var rules: MutableList<RedeliverySummaryRule>) {
    fun validate(item: RedeliverySummary): MutableList<String> {
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

    fun requireValid(item: RedeliverySummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliverySummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliverySummaryValidator(): RedeliverySummaryValidator {
    return RedeliverySummaryValidator(
        mutableListOf<RedeliverySummaryRule>(
            RedeliverySummaryRequiredCodeRule("redelivery summary needs a value"),
            RedeliverySummaryRequiredShiftCodeRule("redelivery summary needs an entry"),
            RedeliverySummaryUnitLengthRule(1000),
        ),
    )
}
