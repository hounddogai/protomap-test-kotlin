package com.acme.transport.customs.clearance.slot

/** A rule a pending clearance record must pass. Check returns its problems, or none. */
interface PendingClearanceRecordRule {
    fun check(item: PendingClearanceRecord): MutableList<String>
}

data class PendingClearanceRecordStartsAtRangeRule(var min: Int, var max: Int) : PendingClearanceRecordRule {
    override fun check(item: PendingClearanceRecord): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceRecordNameLengthRule(var maxLength: Int) : PendingClearanceRecordRule {
    override fun check(item: PendingClearanceRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceRecordScoreRangeRule(var min: Double, var max: Double) : PendingClearanceRecordRule {
    override fun check(item: PendingClearanceRecord): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending clearance record. */
data class PendingClearanceRecordValidator(var rules: MutableList<PendingClearanceRecordRule>) {
    fun validate(item: PendingClearanceRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingClearanceRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingClearanceRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingClearanceRecordValidator(): PendingClearanceRecordValidator {
    return PendingClearanceRecordValidator(
        mutableListOf<PendingClearanceRecordRule>(
            PendingClearanceRecordStartsAtRangeRule(64, 120),
            PendingClearanceRecordNameLengthRule(16),
            PendingClearanceRecordScoreRangeRule(8.0, 2210.7),
        ),
    )
}
