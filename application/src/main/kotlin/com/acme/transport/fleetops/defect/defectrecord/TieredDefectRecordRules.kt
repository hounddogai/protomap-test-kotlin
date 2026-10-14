package com.acme.transport.fleetops.defect.defectrecord

/** A rule a tiered defect record must pass. Check returns its problems, or none. */
interface TieredDefectRecordRule {
    fun check(item: TieredDefectRecord): MutableList<String>
}

data class TieredDefectRecordRequiredTitleRule(var message: String) : TieredDefectRecordRule {
    override fun check(item: TieredDefectRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDefectRecordRequiredSourceRule(var message: String) : TieredDefectRecordRule {
    override fun check(item: TieredDefectRecord): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDefectRecordCodeLengthRule(var maxLength: Int) : TieredDefectRecordRule {
    override fun check(item: TieredDefectRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered defect record. */
data class TieredDefectRecordValidator(var rules: MutableList<TieredDefectRecordRule>) {
    fun validate(item: TieredDefectRecord): MutableList<String> {
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

    fun requireValid(item: TieredDefectRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDefectRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDefectRecordValidator(): TieredDefectRecordValidator {
    return TieredDefectRecordValidator(
        mutableListOf<TieredDefectRecordRule>(
            TieredDefectRecordRequiredTitleRule("tiered defect record needs an entry"),
            TieredDefectRecordRequiredSourceRule("tiered defect record needs a value"),
            TieredDefectRecordCodeLengthRule(120),
        ),
    )
}
