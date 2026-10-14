package com.acme.transport.safety.violation.threshold

/** A rule a violation batch must pass. Check returns its problems, or none. */
interface ViolationBatchRule {
    fun check(item: ViolationBatch): MutableList<String>
}

data class ViolationBatchRequiredCodeRule(var message: String) : ViolationBatchRule {
    override fun check(item: ViolationBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationBatchWindowDaysRangeRule(var min: Int, var max: Int) : ViolationBatchRule {
    override fun check(item: ViolationBatch): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class ViolationBatchRequiredNotesRule(var message: String) : ViolationBatchRule {
    override fun check(item: ViolationBatch): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation batch. */
data class ViolationBatchValidator(var rules: MutableList<ViolationBatchRule>) {
    fun validate(item: ViolationBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationBatchValidator(): ViolationBatchValidator {
    return ViolationBatchValidator(
        mutableListOf<ViolationBatchRule>(
            ViolationBatchRequiredCodeRule("violation batch needs a value"),
            ViolationBatchWindowDaysRangeRule(16, 86400),
            ViolationBatchRequiredNotesRule("violation batch needs this field"),
        ),
    )
}
