package com.acme.transport.lastmile.locker.adjustment

/** A rule a pending locker record must pass. Check returns its problems, or none. */
interface PendingLockerRecordRule {
    fun check(item: PendingLockerRecord): MutableList<String>
}

data class PendingLockerRecordRequiredCodeRule(var message: String) : PendingLockerRecordRule {
    override fun check(item: PendingLockerRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerRecordCodeLengthRule(var maxLength: Int) : PendingLockerRecordRule {
    override fun check(item: PendingLockerRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerRecordScoreRangeRule(var min: Double, var max: Double) : PendingLockerRecordRule {
    override fun check(item: PendingLockerRecord): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class PendingLockerRecordNameLengthRule(var maxLength: Int) : PendingLockerRecordRule {
    override fun check(item: PendingLockerRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerRecordRequiredNameRule(var message: String) : PendingLockerRecordRule {
    override fun check(item: PendingLockerRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending locker record. */
data class PendingLockerRecordValidator(var rules: MutableList<PendingLockerRecordRule>) {
    fun validate(item: PendingLockerRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingLockerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLockerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLockerRecordValidator(): PendingLockerRecordValidator {
    return PendingLockerRecordValidator(
        mutableListOf<PendingLockerRecordRule>(
            PendingLockerRecordRequiredCodeRule("pending locker record needs this field"),
            PendingLockerRecordCodeLengthRule(80),
            PendingLockerRecordScoreRangeRule(3.9, 386.3),
            PendingLockerRecordNameLengthRule(80),
            PendingLockerRecordRequiredNameRule("pending locker record needs this field"),
        ),
    )
}
