package com.acme.transport.crewing.roster.quota

/** A rule a lenient roster batch must pass. Check returns its problems, or none. */
interface LenientRosterBatchRule {
    fun check(item: LenientRosterBatch): MutableList<String>
}

data class LenientRosterBatchCodeLengthRule(var maxLength: Int) : LenientRosterBatchRule {
    override fun check(item: LenientRosterBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterBatchRequiredLaneRule(var message: String) : LenientRosterBatchRule {
    override fun check(item: LenientRosterBatch): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterBatchMarginRangeRule(var min: Double, var max: Double) : LenientRosterBatchRule {
    override fun check(item: LenientRosterBatch): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class LenientRosterBatchUnitLengthRule(var maxLength: Int) : LenientRosterBatchRule {
    override fun check(item: LenientRosterBatch): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterBatchRequiredLabelRule(var message: String) : LenientRosterBatchRule {
    override fun check(item: LenientRosterBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient roster batch. */
data class LenientRosterBatchValidator(var rules: MutableList<LenientRosterBatchRule>) {
    fun validate(item: LenientRosterBatch): MutableList<String> {
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

    fun requireValid(item: LenientRosterBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRosterBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRosterBatchValidator(): LenientRosterBatchValidator {
    return LenientRosterBatchValidator(
        mutableListOf<LenientRosterBatchRule>(
            LenientRosterBatchCodeLengthRule(120),
            LenientRosterBatchRequiredLaneRule("lenient roster batch needs this field"),
            LenientRosterBatchMarginRangeRule(2.1, 4964.0),
            LenientRosterBatchUnitLengthRule(120),
            LenientRosterBatchRequiredLabelRule("lenient roster batch needs this field"),
        ),
    )
}
