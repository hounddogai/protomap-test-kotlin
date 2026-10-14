package com.acme.transport.fleetops.defect.profile

/** A rule a manual defect ledger must pass. Check returns its problems, or none. */
interface ManualDefectLedgerRule {
    fun check(item: ManualDefectLedger): MutableList<String>
}

data class ManualDefectLedgerRequiredRegionRule(var message: String) : ManualDefectLedgerRule {
    override fun check(item: ManualDefectLedger): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectLedgerCodeLengthRule(var maxLength: Int) : ManualDefectLedgerRule {
    override fun check(item: ManualDefectLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDefectLedgerUpdatedAtRangeRule(var min: Int, var max: Int) : ManualDefectLedgerRule {
    override fun check(item: ManualDefectLedger): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual defect ledger. */
data class ManualDefectLedgerValidator(var rules: MutableList<ManualDefectLedgerRule>) {
    fun validate(item: ManualDefectLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDefectLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDefectLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDefectLedgerValidator(): ManualDefectLedgerValidator {
    return ManualDefectLedgerValidator(
        mutableListOf<ManualDefectLedgerRule>(
            ManualDefectLedgerRequiredRegionRule("manual defect ledger needs a value"),
            ManualDefectLedgerCodeLengthRule(120),
            ManualDefectLedgerUpdatedAtRangeRule(32, 255),
        ),
    )
}
