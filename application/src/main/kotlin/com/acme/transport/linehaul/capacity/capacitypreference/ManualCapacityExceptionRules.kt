package com.acme.transport.linehaul.capacity.capacitypreference

/** A rule a manual capacity exception must pass. Check returns its problems, or none. */
interface ManualCapacityExceptionRule {
    fun check(item: ManualCapacityException): MutableList<String>
}

data class ManualCapacityExceptionRevisionRangeRule(var min: Int, var max: Int) : ManualCapacityExceptionRule {
    override fun check(item: ManualCapacityException): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualCapacityExceptionCodeLengthRule(var maxLength: Int) : ManualCapacityExceptionRule {
    override fun check(item: ManualCapacityException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCapacityExceptionRequiredLabelRule(var message: String) : ManualCapacityExceptionRule {
    override fun check(item: ManualCapacityException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual capacity exception. */
data class ManualCapacityExceptionValidator(var rules: MutableList<ManualCapacityExceptionRule>) {
    fun validate(item: ManualCapacityException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualCapacityException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCapacityException>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCapacityExceptionValidator(): ManualCapacityExceptionValidator {
    return ManualCapacityExceptionValidator(
        mutableListOf<ManualCapacityExceptionRule>(
            ManualCapacityExceptionRevisionRangeRule(1000, 86400),
            ManualCapacityExceptionCodeLengthRule(64),
            ManualCapacityExceptionRequiredLabelRule("manual capacity exception needs a value"),
        ),
    )
}
