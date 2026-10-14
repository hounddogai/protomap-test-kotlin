package com.acme.transport.customs.hold.recorditem

/** A rule a hold record must pass. Check returns its problems, or none. */
interface HoldRecordRule {
    fun check(item: HoldRecord): MutableList<String>
}

data class HoldRecordRequiredNameRule(var message: String) : HoldRecordRule {
    override fun check(item: HoldRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldRecordSummaryLengthRule(var maxLength: Int) : HoldRecordRule {
    override fun check(item: HoldRecord): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldRecordCapacityRangeRule(var min: Int, var max: Int) : HoldRecordRule {
    override fun check(item: HoldRecord): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold record. */
data class HoldRecordValidator(var rules: MutableList<HoldRecordRule>) {
    fun validate(item: HoldRecord): MutableList<String> {
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

    fun requireValid(item: HoldRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldRecordValidator(): HoldRecordValidator {
    return HoldRecordValidator(
        mutableListOf<HoldRecordRule>(
            HoldRecordRequiredNameRule("hold record needs a value"),
            HoldRecordSummaryLengthRule(86400),
            HoldRecordCapacityRangeRule(32, 64),
        ),
    )
}
