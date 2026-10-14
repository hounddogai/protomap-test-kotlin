package com.acme.transport.lastmile.drop.recorditem

/** A rule a drop record must pass. Check returns its problems, or none. */
interface DropRecordRule {
    fun check(item: DropRecord): MutableList<String>
}

data class DropRecordThresholdRangeRule(var min: Double, var max: Double) : DropRecordRule {
    override fun check(item: DropRecord): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class DropRecordRequiredUnitRule(var message: String) : DropRecordRule {
    override fun check(item: DropRecord): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropRecordMaximumRangeRule(var min: Double, var max: Double) : DropRecordRule {
    override fun check(item: DropRecord): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class DropRecordUnitLengthRule(var maxLength: Int) : DropRecordRule {
    override fun check(item: DropRecord): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop record. */
data class DropRecordValidator(var rules: MutableList<DropRecordRule>) {
    fun validate(item: DropRecord): MutableList<String> {
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

    fun requireValid(item: DropRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultDropRecordValidator(): DropRecordValidator {
    return DropRecordValidator(
        mutableListOf<DropRecordRule>(
            DropRecordThresholdRangeRule(3.4, 4174.0),
            DropRecordRequiredUnitRule("drop record needs an entry"),
            DropRecordMaximumRangeRule(9.1, 994.7),
            DropRecordUnitLengthRule(32),
        ),
    )
}
