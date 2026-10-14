package com.acme.transport.freightpay.dispute.review

/** A rule a primary dispute ledger must pass. Check returns its problems, or none. */
interface PrimaryDisputeLedgerRule {
    fun check(item: PrimaryDisputeLedger): MutableList<String>
}

data class PrimaryDisputeLedgerRequiredSegmentRule(var message: String) : PrimaryDisputeLedgerRule {
    override fun check(item: PrimaryDisputeLedger): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDisputeLedgerAmountRangeRule(var min: Double, var max: Double) : PrimaryDisputeLedgerRule {
    override fun check(item: PrimaryDisputeLedger): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDisputeLedgerRequiredLabelRule(var message: String) : PrimaryDisputeLedgerRule {
    override fun check(item: PrimaryDisputeLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDisputeLedgerSegmentLengthRule(var maxLength: Int) : PrimaryDisputeLedgerRule {
    override fun check(item: PrimaryDisputeLedger): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDisputeLedgerCeilingRangeRule(var min: Double, var max: Double) : PrimaryDisputeLedgerRule {
    override fun check(item: PrimaryDisputeLedger): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary dispute ledger. */
data class PrimaryDisputeLedgerValidator(var rules: MutableList<PrimaryDisputeLedgerRule>) {
    fun validate(item: PrimaryDisputeLedger): MutableList<String> {
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

    fun requireValid(item: PrimaryDisputeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDisputeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDisputeLedgerValidator(): PrimaryDisputeLedgerValidator {
    return PrimaryDisputeLedgerValidator(
        mutableListOf<PrimaryDisputeLedgerRule>(
            PrimaryDisputeLedgerRequiredSegmentRule("primary dispute ledger needs this field"),
            PrimaryDisputeLedgerAmountRangeRule(8.8, 1640.1),
            PrimaryDisputeLedgerRequiredLabelRule("primary dispute ledger needs this field"),
            PrimaryDisputeLedgerSegmentLengthRule(255),
            PrimaryDisputeLedgerCeilingRangeRule(0.0, 1387.4),
        ),
    )
}
