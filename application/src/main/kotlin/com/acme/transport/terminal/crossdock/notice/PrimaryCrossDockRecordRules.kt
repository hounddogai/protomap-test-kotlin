package com.acme.transport.terminal.crossdock.notice

/** A rule a primary cross dock record must pass. Check returns its problems, or none. */
interface PrimaryCrossDockRecordRule {
    fun check(item: PrimaryCrossDockRecord): MutableList<String>
}

data class PrimaryCrossDockRecordDriverIdLengthRule(var maxLength: Int) : PrimaryCrossDockRecordRule {
    override fun check(item: PrimaryCrossDockRecord): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockRecordReviewerLengthRule(var maxLength: Int) : PrimaryCrossDockRecordRule {
    override fun check(item: PrimaryCrossDockRecord): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockRecordCeilingRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryCrossDockRecordRule {
    override fun check(item: PrimaryCrossDockRecord): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class PrimaryCrossDockRecordMinimumRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryCrossDockRecordRule {
    override fun check(item: PrimaryCrossDockRecord): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockRecordRequiredDriverIdRule(var message: String) : PrimaryCrossDockRecordRule {
    override fun check(item: PrimaryCrossDockRecord): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary cross dock record. */
data class PrimaryCrossDockRecordValidator(var rules: MutableList<PrimaryCrossDockRecordRule>) {
    fun validate(item: PrimaryCrossDockRecord): MutableList<String> {
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

    fun requireValid(item: PrimaryCrossDockRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCrossDockRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCrossDockRecordValidator(): PrimaryCrossDockRecordValidator {
    return PrimaryCrossDockRecordValidator(
        mutableListOf<PrimaryCrossDockRecordRule>(
            PrimaryCrossDockRecordDriverIdLengthRule(255),
            PrimaryCrossDockRecordReviewerLengthRule(32),
            PrimaryCrossDockRecordCeilingRangeRule(7.9, 2002.5),
            PrimaryCrossDockRecordMinimumRangeRule(3.4, 4536.0),
            PrimaryCrossDockRecordRequiredDriverIdRule("primary cross dock record needs an entry"),
        ),
    )
}
