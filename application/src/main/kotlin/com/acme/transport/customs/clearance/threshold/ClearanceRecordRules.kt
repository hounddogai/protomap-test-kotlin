package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.text.normalize

/** A rule a clearance record must pass. Check returns its problems, or none. */
interface ClearanceRecordRule {
    fun check(item: ClearanceRecord): MutableList<String>
}

data class ClearanceRecordLaneLengthRule(var maxLength: Int) : ClearanceRecordRule {
    override fun check(item: ClearanceRecord): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRecordTitleLengthRule(var maxLength: Int) : ClearanceRecordRule {
    override fun check(item: ClearanceRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRecordExpiresAtRangeRule(var min: Int, var max: Int) : ClearanceRecordRule {
    override fun check(item: ClearanceRecord): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRecordRequiredSegmentRule(var message: String) : ClearanceRecordRule {
    override fun check(item: ClearanceRecord): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRecordAllowedSegmentRule(var allowed: MutableList<String>) : ClearanceRecordRule {
    override fun check(item: ClearanceRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRecordRequiredTitleRule(var message: String) : ClearanceRecordRule {
    override fun check(item: ClearanceRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance record. */
data class ClearanceRecordValidator(var rules: MutableList<ClearanceRecordRule>) {
    fun validate(item: ClearanceRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceRecordValidator(): ClearanceRecordValidator {
    return ClearanceRecordValidator(
        mutableListOf<ClearanceRecordRule>(
            ClearanceRecordLaneLengthRule(16),
            ClearanceRecordTitleLengthRule(80),
            ClearanceRecordExpiresAtRangeRule(120, 1000),
            ClearanceRecordRequiredSegmentRule("clearance record needs a value"),
            ClearanceRecordAllowedSegmentRule(mutableListOf<String>()),
            ClearanceRecordRequiredTitleRule("clearance record needs this field"),
        ),
    )
}
