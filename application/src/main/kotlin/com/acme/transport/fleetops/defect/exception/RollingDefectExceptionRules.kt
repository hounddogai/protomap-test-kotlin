package com.acme.transport.fleetops.defect.exception

/** A rule a rolling defect exception must pass. Check returns its problems, or none. */
interface RollingDefectExceptionRule {
    fun check(item: RollingDefectException): MutableList<String>
}

data class RollingDefectExceptionRequiredTitleRule(var message: String) : RollingDefectExceptionRule {
    override fun check(item: RollingDefectException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectExceptionScoreRangeRule(var min: Double, var max: Double) : RollingDefectExceptionRule {
    override fun check(item: RollingDefectException): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectExceptionCarrierLengthRule(var maxLength: Int) : RollingDefectExceptionRule {
    override fun check(item: RollingDefectException): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling defect exception. */
data class RollingDefectExceptionValidator(var rules: MutableList<RollingDefectExceptionRule>) {
    fun validate(item: RollingDefectException): MutableList<String> {
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

    fun requireValid(item: RollingDefectException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDefectException>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDefectExceptionValidator(): RollingDefectExceptionValidator {
    return RollingDefectExceptionValidator(
        mutableListOf<RollingDefectExceptionRule>(
            RollingDefectExceptionRequiredTitleRule("rolling defect exception needs an entry"),
            RollingDefectExceptionScoreRangeRule(4.9, 2864.2),
            RollingDefectExceptionCarrierLengthRule(255),
        ),
    )
}
