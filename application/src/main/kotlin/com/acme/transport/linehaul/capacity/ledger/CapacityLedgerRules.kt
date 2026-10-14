package com.acme.transport.linehaul.capacity.ledger

/** A rule a capacity ledger must pass. Check returns its problems, or none. */
interface CapacityLedgerRule {
    fun check(item: CapacityLedger): MutableList<String>
}

data class CapacityLedgerRequiredLabelRule(var message: String) : CapacityLedgerRule {
    override fun check(item: CapacityLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityLedgerPhoneLengthRule(var maxLength: Int) : CapacityLedgerRule {
    override fun check(item: CapacityLedger): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityLedgerRevisionRangeRule(var min: Int, var max: Int) : CapacityLedgerRule {
    override fun check(item: CapacityLedger): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity ledger. */
data class CapacityLedgerValidator(var rules: MutableList<CapacityLedgerRule>) {
    fun validate(item: CapacityLedger): MutableList<String> {
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

    fun requireValid(item: CapacityLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityLedgerValidator(): CapacityLedgerValidator {
    return CapacityLedgerValidator(
        mutableListOf<CapacityLedgerRule>(
            CapacityLedgerRequiredLabelRule("capacity ledger needs this field"),
            CapacityLedgerPhoneLengthRule(1000),
            CapacityLedgerRevisionRangeRule(80, 80),
        ),
    )
}
