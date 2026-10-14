package com.acme.transport.lastmile.attempt.bundle

/** A rule a rolling attempt record must pass. Check returns its problems, or none. */
interface RollingAttemptRecordRule {
    fun check(item: RollingAttemptRecord): MutableList<String>
}

data class RollingAttemptRecordCodeLengthRule(var maxLength: Int) : RollingAttemptRecordRule {
    override fun check(item: RollingAttemptRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAttemptRecordActiveGuardRule(var limit: Double) : RollingAttemptRecordRule {
    override fun check(item: RollingAttemptRecord): MutableList<String> {
        if (item.active && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RollingAttemptRecordRequiredCodeRule(var message: String) : RollingAttemptRecordRule {
    override fun check(item: RollingAttemptRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling attempt record. */
data class RollingAttemptRecordValidator(var rules: MutableList<RollingAttemptRecordRule>) {
    fun validate(item: RollingAttemptRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingAttemptRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAttemptRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAttemptRecordValidator(): RollingAttemptRecordValidator {
    return RollingAttemptRecordValidator(
        mutableListOf<RollingAttemptRecordRule>(
            RollingAttemptRecordCodeLengthRule(16),
            RollingAttemptRecordActiveGuardRule(8.2),
            RollingAttemptRecordRequiredCodeRule("rolling attempt record needs a value"),
        ),
    )
}
