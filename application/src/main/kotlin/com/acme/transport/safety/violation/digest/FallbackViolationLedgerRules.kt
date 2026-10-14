package com.acme.transport.safety.violation.digest

/** A rule a fallback violation ledger must pass. Check returns its problems, or none. */
interface FallbackViolationLedgerRule {
    fun check(item: FallbackViolationLedger): MutableList<String>
}

data class FallbackViolationLedgerLabelLengthRule(var maxLength: Int) : FallbackViolationLedgerRule {
    override fun check(item: FallbackViolationLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackViolationLedgerRequiredLabelRule(var message: String) : FallbackViolationLedgerRule {
    override fun check(item: FallbackViolationLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackViolationLedgerCapacityRangeRule(var min: Int, var max: Int) : FallbackViolationLedgerRule {
    override fun check(item: FallbackViolationLedger): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback violation ledger. */
data class FallbackViolationLedgerValidator(var rules: MutableList<FallbackViolationLedgerRule>) {
    fun validate(item: FallbackViolationLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackViolationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackViolationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackViolationLedgerValidator(): FallbackViolationLedgerValidator {
    return FallbackViolationLedgerValidator(
        mutableListOf<FallbackViolationLedgerRule>(
            FallbackViolationLedgerLabelLengthRule(64),
            FallbackViolationLedgerRequiredLabelRule("fallback violation ledger needs an entry"),
            FallbackViolationLedgerCapacityRangeRule(16, 255),
        ),
    )
}
