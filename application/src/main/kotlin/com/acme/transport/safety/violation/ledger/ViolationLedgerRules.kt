package com.acme.transport.safety.violation.ledger

import com.acme.transport.kit.text.normalize

/** A rule a violation ledger must pass. Check returns its problems, or none. */
interface ViolationLedgerRule {
    fun check(item: ViolationLedger): MutableList<String>
}

data class ViolationLedgerQuantityRangeRule(var min: Int, var max: Int) : ViolationLedgerRule {
    override fun check(item: ViolationLedger): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ViolationLedgerMinimumRangeRule(var min: Double, var max: Double) : ViolationLedgerRule {
    override fun check(item: ViolationLedger): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class ViolationLedgerRequiredCountryRule(var message: String) : ViolationLedgerRule {
    override fun check(item: ViolationLedger): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationLedgerAllowedShiftCodeRule(var allowed: MutableList<String>) : ViolationLedgerRule {
    override fun check(item: ViolationLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation ledger. */
data class ViolationLedgerValidator(var rules: MutableList<ViolationLedgerRule>) {
    fun validate(item: ViolationLedger): MutableList<String> {
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

    fun requireValid(item: ViolationLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationLedgerValidator(): ViolationLedgerValidator {
    return ViolationLedgerValidator(
        mutableListOf<ViolationLedgerRule>(
            ViolationLedgerQuantityRangeRule(120, 120),
            ViolationLedgerMinimumRangeRule(3.2, 3952.3),
            ViolationLedgerRequiredCountryRule("violation ledger needs a value"),
            ViolationLedgerAllowedShiftCodeRule(mutableListOf<String>("adaptive")),
        ),
    )
}
