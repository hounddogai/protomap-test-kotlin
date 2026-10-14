package com.acme.transport.fleetops.tire.tier

/** A rule a manual tire ledger must pass. Check returns its problems, or none. */
interface ManualTireLedgerRule {
    fun check(item: ManualTireLedger): MutableList<String>
}

data class ManualTireLedgerRequiredLabelRule(var message: String) : ManualTireLedgerRule {
    override fun check(item: ManualTireLedger): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTireLedgerCodeLengthRule(var maxLength: Int) : ManualTireLedgerRule {
    override fun check(item: ManualTireLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTireLedgerRatioRangeRule(var min: Double, var max: Double) : ManualTireLedgerRule {
    override fun check(item: ManualTireLedger): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual tire ledger. */
data class ManualTireLedgerValidator(var rules: MutableList<ManualTireLedgerRule>) {
    fun validate(item: ManualTireLedger): MutableList<String> {
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

    fun requireValid(item: ManualTireLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTireLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTireLedgerValidator(): ManualTireLedgerValidator {
    return ManualTireLedgerValidator(
        mutableListOf<ManualTireLedgerRule>(
            ManualTireLedgerRequiredLabelRule("manual tire ledger needs a value"),
            ManualTireLedgerCodeLengthRule(255),
            ManualTireLedgerRatioRangeRule(0.7, 698.8),
        ),
    )
}
