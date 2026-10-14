package com.acme.transport.network.transittime.recorditem

/** A rule a transit time record must pass. Check returns its problems, or none. */
interface TransitTimeRecordRule {
    fun check(item: TransitTimeRecord): MutableList<String>
}

data class TransitTimeRecordContactEmailLengthRule(var maxLength: Int) : TransitTimeRecordRule {
    override fun check(item: TransitTimeRecord): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeRecordRequiredCodeRule(var message: String) : TransitTimeRecordRule {
    override fun check(item: TransitTimeRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeRecordCeilingRangeRule(var min: Double, var max: Double) : TransitTimeRecordRule {
    override fun check(item: TransitTimeRecord): MutableList<String> {
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

/** Runs every rule of a transit time record. */
data class TransitTimeRecordValidator(var rules: MutableList<TransitTimeRecordRule>) {
    fun validate(item: TransitTimeRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeRecordValidator(): TransitTimeRecordValidator {
    return TransitTimeRecordValidator(
        mutableListOf<TransitTimeRecordRule>(
            TransitTimeRecordContactEmailLengthRule(80),
            TransitTimeRecordRequiredCodeRule("transit time record needs an entry"),
            TransitTimeRecordCeilingRangeRule(2.7, 3032.7),
        ),
    )
}
