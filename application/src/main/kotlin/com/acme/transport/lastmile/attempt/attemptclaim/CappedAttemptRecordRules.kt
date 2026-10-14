package com.acme.transport.lastmile.attempt.attemptclaim

/** A rule a capped attempt record must pass. Check returns its problems, or none. */
interface CappedAttemptRecordRule {
    fun check(item: CappedAttemptRecord): MutableList<String>
}

data class CappedAttemptRecordLaneLengthRule(var maxLength: Int) : CappedAttemptRecordRule {
    override fun check(item: CappedAttemptRecord): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptRecordQuantityRangeRule(var min: Int, var max: Int) : CappedAttemptRecordRule {
    override fun check(item: CappedAttemptRecord): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptRecordRequiredNameRule(var message: String) : CappedAttemptRecordRule {
    override fun check(item: CappedAttemptRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptRecordNameLengthRule(var maxLength: Int) : CappedAttemptRecordRule {
    override fun check(item: CappedAttemptRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptRecordCodeLengthRule(var maxLength: Int) : CappedAttemptRecordRule {
    override fun check(item: CappedAttemptRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedAttemptRecordRequiredCodeRule(var message: String) : CappedAttemptRecordRule {
    override fun check(item: CappedAttemptRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped attempt record. */
data class CappedAttemptRecordValidator(var rules: MutableList<CappedAttemptRecordRule>) {
    fun validate(item: CappedAttemptRecord): MutableList<String> {
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

    fun requireValid(item: CappedAttemptRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAttemptRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAttemptRecordValidator(): CappedAttemptRecordValidator {
    return CappedAttemptRecordValidator(
        mutableListOf<CappedAttemptRecordRule>(
            CappedAttemptRecordLaneLengthRule(3600),
            CappedAttemptRecordQuantityRangeRule(32, 255),
            CappedAttemptRecordRequiredNameRule("capped attempt record needs this field"),
            CappedAttemptRecordNameLengthRule(3600),
            CappedAttemptRecordCodeLengthRule(32),
            CappedAttemptRecordRequiredCodeRule("capped attempt record needs a value"),
        ),
    )
}
