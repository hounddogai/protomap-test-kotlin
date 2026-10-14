package com.acme.transport.safety.violation.threshold

/** A rule a violation threshold must pass. Check returns its problems, or none. */
interface ViolationThresholdRule {
    fun check(item: ViolationThreshold): MutableList<String>
}

data class ViolationThresholdStartsAtRangeRule(var min: Int, var max: Int) : ViolationThresholdRule {
    override fun check(item: ViolationThreshold): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationThresholdTitleLengthRule(var maxLength: Int) : ViolationThresholdRule {
    override fun check(item: ViolationThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationThresholdNameLengthRule(var maxLength: Int) : ViolationThresholdRule {
    override fun check(item: ViolationThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation threshold. */
data class ViolationThresholdValidator(var rules: MutableList<ViolationThresholdRule>) {
    fun validate(item: ViolationThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationThresholdValidator(): ViolationThresholdValidator {
    return ViolationThresholdValidator(
        mutableListOf<ViolationThresholdRule>(
            ViolationThresholdStartsAtRangeRule(80, 255),
            ViolationThresholdTitleLengthRule(32),
            ViolationThresholdNameLengthRule(32),
        ),
    )
}
