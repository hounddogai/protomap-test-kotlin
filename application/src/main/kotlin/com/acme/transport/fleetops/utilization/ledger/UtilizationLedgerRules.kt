package com.acme.transport.fleetops.utilization.ledger

/** A rule a utilization ledger must pass. Check returns its problems, or none. */
interface UtilizationLedgerRule {
    fun check(item: UtilizationLedger): MutableList<String>
}

data class UtilizationLedgerDiscountRangeRule(var min: Double, var max: Double) : UtilizationLedgerRule {
    override fun check(item: UtilizationLedger): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class UtilizationLedgerNotesLengthRule(var maxLength: Int) : UtilizationLedgerRule {
    override fun check(item: UtilizationLedger): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationLedgerTitleLengthRule(var maxLength: Int) : UtilizationLedgerRule {
    override fun check(item: UtilizationLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationLedgerRequiredTitleRule(var message: String) : UtilizationLedgerRule {
    override fun check(item: UtilizationLedger): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationLedgerRegionLengthRule(var maxLength: Int) : UtilizationLedgerRule {
    override fun check(item: UtilizationLedger): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization ledger. */
data class UtilizationLedgerValidator(var rules: MutableList<UtilizationLedgerRule>) {
    fun validate(item: UtilizationLedger): MutableList<String> {
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

    fun requireValid(item: UtilizationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationLedgerValidator(): UtilizationLedgerValidator {
    return UtilizationLedgerValidator(
        mutableListOf<UtilizationLedgerRule>(
            UtilizationLedgerDiscountRangeRule(0.6, 4958.4),
            UtilizationLedgerNotesLengthRule(255),
            UtilizationLedgerTitleLengthRule(32),
            UtilizationLedgerRequiredTitleRule("utilization ledger needs a value"),
            UtilizationLedgerRegionLengthRule(3600),
        ),
    )
}
