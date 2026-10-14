package com.acme.transport.customs.clearance.request

/** A rule a legacy clearance window must pass. Check returns its problems, or none. */
interface LegacyClearanceWindowRule {
    fun check(item: LegacyClearanceWindow): MutableList<String>
}

data class LegacyClearanceWindowLabelLengthRule(var maxLength: Int) : LegacyClearanceWindowRule {
    override fun check(item: LegacyClearanceWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyClearanceWindowUpdatedAtRangeRule(var min: Int, var max: Int) : LegacyClearanceWindowRule {
    override fun check(item: LegacyClearanceWindow): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class LegacyClearanceWindowCodeLengthRule(var maxLength: Int) : LegacyClearanceWindowRule {
    override fun check(item: LegacyClearanceWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy clearance window. */
data class LegacyClearanceWindowValidator(var rules: MutableList<LegacyClearanceWindowRule>) {
    fun validate(item: LegacyClearanceWindow): MutableList<String> {
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

    fun requireValid(item: LegacyClearanceWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyClearanceWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyClearanceWindowValidator(): LegacyClearanceWindowValidator {
    return LegacyClearanceWindowValidator(
        mutableListOf<LegacyClearanceWindowRule>(
            LegacyClearanceWindowLabelLengthRule(1000),
            LegacyClearanceWindowUpdatedAtRangeRule(16, 1000),
            LegacyClearanceWindowCodeLengthRule(3600),
        ),
    )
}
