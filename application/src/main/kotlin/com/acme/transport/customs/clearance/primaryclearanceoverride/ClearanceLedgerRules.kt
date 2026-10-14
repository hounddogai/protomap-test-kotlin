package com.acme.transport.customs.clearance.primaryclearanceoverride

/** A rule a clearance ledger must pass. Check returns its problems, or none. */
interface ClearanceLedgerRule {
    fun check(item: ClearanceLedger): MutableList<String>
}

data class ClearanceLedgerRequiredCodeRule(var message: String) : ClearanceLedgerRule {
    override fun check(item: ClearanceLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceLedgerDiscountRangeRule(var min: Double, var max: Double) : ClearanceLedgerRule {
    override fun check(item: ClearanceLedger): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceLedgerCodeLengthRule(var maxLength: Int) : ClearanceLedgerRule {
    override fun check(item: ClearanceLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceLedgerRequiredDockRule(var message: String) : ClearanceLedgerRule {
    override fun check(item: ClearanceLedger): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceLedgerStopCountRangeRule(var min: Int, var max: Int) : ClearanceLedgerRule {
    override fun check(item: ClearanceLedger): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceLedgerSegmentLengthRule(var maxLength: Int) : ClearanceLedgerRule {
    override fun check(item: ClearanceLedger): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance ledger. */
data class ClearanceLedgerValidator(var rules: MutableList<ClearanceLedgerRule>) {
    fun validate(item: ClearanceLedger): MutableList<String> {
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

    fun requireValid(item: ClearanceLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceLedgerValidator(): ClearanceLedgerValidator {
    return ClearanceLedgerValidator(
        mutableListOf<ClearanceLedgerRule>(
            ClearanceLedgerRequiredCodeRule("clearance ledger needs a value"),
            ClearanceLedgerDiscountRangeRule(5.0, 2303.1),
            ClearanceLedgerCodeLengthRule(255),
            ClearanceLedgerRequiredDockRule("clearance ledger needs a value"),
            ClearanceLedgerStopCountRangeRule(80, 255),
            ClearanceLedgerSegmentLengthRule(86400),
        ),
    )
}
