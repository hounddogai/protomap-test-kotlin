package com.acme.transport.fleetops.defect.forecast

/** A rule a defect ledger must pass. Check returns its problems, or none. */
interface DefectLedgerRule {
    fun check(item: DefectLedger): MutableList<String>
}

data class DefectLedgerSequenceRangeRule(var min: Int, var max: Int) : DefectLedgerRule {
    override fun check(item: DefectLedger): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectLedgerArchivedGuardRule(var limit: Int) : DefectLedgerRule {
    override fun check(item: DefectLedger): MutableList<String> {
        if (item.archived && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class DefectLedgerOdometerRangeRule(var min: Int, var max: Int) : DefectLedgerRule {
    override fun check(item: DefectLedger): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectLedgerTitleLengthRule(var maxLength: Int) : DefectLedgerRule {
    override fun check(item: DefectLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect ledger. */
data class DefectLedgerValidator(var rules: MutableList<DefectLedgerRule>) {
    fun validate(item: DefectLedger): MutableList<String> {
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

    fun requireValid(item: DefectLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectLedgerValidator(): DefectLedgerValidator {
    return DefectLedgerValidator(
        mutableListOf<DefectLedgerRule>(
            DefectLedgerSequenceRangeRule(1000, 86400),
            DefectLedgerArchivedGuardRule(16),
            DefectLedgerOdometerRangeRule(64, 255),
            DefectLedgerTitleLengthRule(255),
        ),
    )
}
