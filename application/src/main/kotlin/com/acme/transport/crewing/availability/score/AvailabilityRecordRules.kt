package com.acme.transport.crewing.availability.score

/** A rule a availability record must pass. Check returns its problems, or none. */
interface AvailabilityRecordRule {
    fun check(item: AvailabilityRecord): MutableList<String>
}

data class AvailabilityRecordMinimumRangeRule(var min: Double, var max: Double) : AvailabilityRecordRule {
    override fun check(item: AvailabilityRecord): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class AvailabilityRecordRequiredCodeRule(var message: String) : AvailabilityRecordRule {
    override fun check(item: AvailabilityRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRecordRequiredTitleRule(var message: String) : AvailabilityRecordRule {
    override fun check(item: AvailabilityRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability record. */
data class AvailabilityRecordValidator(var rules: MutableList<AvailabilityRecordRule>) {
    fun validate(item: AvailabilityRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityRecordValidator(): AvailabilityRecordValidator {
    return AvailabilityRecordValidator(
        mutableListOf<AvailabilityRecordRule>(
            AvailabilityRecordMinimumRangeRule(4.7, 2693.6),
            AvailabilityRecordRequiredCodeRule("availability record needs this field"),
            AvailabilityRecordRequiredTitleRule("availability record needs a value"),
        ),
    )
}
