package com.acme.transport.safety.violation.proposal

/** A rule a default violation segment must pass. Check returns its problems, or none. */
interface DefaultViolationSegmentRule {
    fun check(item: DefaultViolationSegment): MutableList<String>
}

data class DefaultViolationSegmentCapacityRangeRule(var min: Int, var max: Int) : DefaultViolationSegmentRule {
    override fun check(item: DefaultViolationSegment): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultViolationSegmentNameLengthRule(var maxLength: Int) : DefaultViolationSegmentRule {
    override fun check(item: DefaultViolationSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultViolationSegmentCodeLengthRule(var maxLength: Int) : DefaultViolationSegmentRule {
    override fun check(item: DefaultViolationSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultViolationSegmentRequiredNameRule(var message: String) : DefaultViolationSegmentRule {
    override fun check(item: DefaultViolationSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultViolationSegmentCodePrefixRule(var prefix: String) : DefaultViolationSegmentRule {
    override fun check(item: DefaultViolationSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultViolationSegmentRequiredCodeRule(var message: String) : DefaultViolationSegmentRule {
    override fun check(item: DefaultViolationSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default violation segment. */
data class DefaultViolationSegmentValidator(var rules: MutableList<DefaultViolationSegmentRule>) {
    fun validate(item: DefaultViolationSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultViolationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultViolationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultViolationSegmentValidator(): DefaultViolationSegmentValidator {
    return DefaultViolationSegmentValidator(
        mutableListOf<DefaultViolationSegmentRule>(
            DefaultViolationSegmentCapacityRangeRule(255, 86400),
            DefaultViolationSegmentNameLengthRule(120),
            DefaultViolationSegmentCodeLengthRule(16),
            DefaultViolationSegmentRequiredNameRule("default violation segment needs this field"),
            DefaultViolationSegmentCodePrefixRule("Q"),
            DefaultViolationSegmentRequiredCodeRule("default violation segment needs this field"),
        ),
    )
}
