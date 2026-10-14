package com.acme.transport.safety.violation.batch

/** A rule a lenient violation cycle must pass. Check returns its problems, or none. */
interface LenientViolationCycleRule {
    fun check(item: LenientViolationCycle): MutableList<String>
}

data class LenientViolationCycleCodeLengthRule(var maxLength: Int) : LenientViolationCycleRule {
    override fun check(item: LenientViolationCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationCycleRequiredCodeRule(var message: String) : LenientViolationCycleRule {
    override fun check(item: LenientViolationCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientViolationCycleRequiredLabelRule(var message: String) : LenientViolationCycleRule {
    override fun check(item: LenientViolationCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient violation cycle. */
data class LenientViolationCycleValidator(var rules: MutableList<LenientViolationCycleRule>) {
    fun validate(item: LenientViolationCycle): MutableList<String> {
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

    fun requireValid(item: LenientViolationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientViolationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientViolationCycleValidator(): LenientViolationCycleValidator {
    return LenientViolationCycleValidator(
        mutableListOf<LenientViolationCycleRule>(
            LenientViolationCycleCodeLengthRule(86400),
            LenientViolationCycleRequiredCodeRule("lenient violation cycle needs a value"),
            LenientViolationCycleRequiredLabelRule("lenient violation cycle needs this field"),
        ),
    )
}
