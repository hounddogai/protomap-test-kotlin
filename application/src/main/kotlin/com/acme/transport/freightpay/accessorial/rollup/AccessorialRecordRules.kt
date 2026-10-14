package com.acme.transport.freightpay.accessorial.rollup

/** A rule a accessorial record must pass. Check returns its problems, or none. */
interface AccessorialRecordRule {
    fun check(item: AccessorialRecord): MutableList<String>
}

data class AccessorialRecordUnitLengthRule(var maxLength: Int) : AccessorialRecordRule {
    override fun check(item: AccessorialRecord): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialRecordRequiredLabelRule(var message: String) : AccessorialRecordRule {
    override fun check(item: AccessorialRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialRecordCreatedAtRangeRule(var min: Int, var max: Int) : AccessorialRecordRule {
    override fun check(item: AccessorialRecord): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial record. */
data class AccessorialRecordValidator(var rules: MutableList<AccessorialRecordRule>) {
    fun validate(item: AccessorialRecord): MutableList<String> {
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

    fun requireValid(item: AccessorialRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialRecordValidator(): AccessorialRecordValidator {
    return AccessorialRecordValidator(
        mutableListOf<AccessorialRecordRule>(
            AccessorialRecordUnitLengthRule(64),
            AccessorialRecordRequiredLabelRule("accessorial record needs this field"),
            AccessorialRecordCreatedAtRangeRule(16, 86400),
        ),
    )
}
