package com.acme.transport.freightpay.fuelsurcharge.baseline

import com.acme.transport.kit.text.normalize

/** A rule a rolling fuel surcharge ledger must pass. Check returns its problems, or none. */
interface RollingFuelSurchargeLedgerRule {
    fun check(item: RollingFuelSurchargeLedger): MutableList<String>
}

data class RollingFuelSurchargeLedgerRequiredCodeRule(var message: String) : RollingFuelSurchargeLedgerRule {
    override fun check(item: RollingFuelSurchargeLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelSurchargeLedgerLabelLengthRule(var maxLength: Int) : RollingFuelSurchargeLedgerRule {
    override fun check(item: RollingFuelSurchargeLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelSurchargeLedgerAllowedDockRule(
    var allowed: MutableList<String>,
) : RollingFuelSurchargeLedgerRule {
    override fun check(item: RollingFuelSurchargeLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelSurchargeLedgerRequiredSummaryRule(var message: String) : RollingFuelSurchargeLedgerRule {
    override fun check(item: RollingFuelSurchargeLedger): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelSurchargeLedgerTimezoneLengthRule(var maxLength: Int) : RollingFuelSurchargeLedgerRule {
    override fun check(item: RollingFuelSurchargeLedger): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling fuel surcharge ledger. */
data class RollingFuelSurchargeLedgerValidator(var rules: MutableList<RollingFuelSurchargeLedgerRule>) {
    fun validate(item: RollingFuelSurchargeLedger): MutableList<String> {
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

    fun requireValid(item: RollingFuelSurchargeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFuelSurchargeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFuelSurchargeLedgerValidator(): RollingFuelSurchargeLedgerValidator {
    return RollingFuelSurchargeLedgerValidator(
        mutableListOf<RollingFuelSurchargeLedgerRule>(
            RollingFuelSurchargeLedgerRequiredCodeRule("rolling fuel surcharge ledger needs this field"),
            RollingFuelSurchargeLedgerLabelLengthRule(64),
            RollingFuelSurchargeLedgerAllowedDockRule(mutableListOf<String>("default", "regional")),
            RollingFuelSurchargeLedgerRequiredSummaryRule("rolling fuel surcharge ledger needs this field"),
            RollingFuelSurchargeLedgerTimezoneLengthRule(255),
        ),
    )
}
