package com.acme.transport.safety.violation.preference

/** A rule a primary violation tier must pass. Check returns its problems, or none. */
interface PrimaryViolationTierRule {
    fun check(item: PrimaryViolationTier): MutableList<String>
}

data class PrimaryViolationTierGraceDaysRangeRule(var min: Int, var max: Int) : PrimaryViolationTierRule {
    override fun check(item: PrimaryViolationTier): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class PrimaryViolationTierRequiredCodeRule(var message: String) : PrimaryViolationTierRule {
    override fun check(item: PrimaryViolationTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryViolationTierRequiredSegmentRule(var message: String) : PrimaryViolationTierRule {
    override fun check(item: PrimaryViolationTier): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryViolationTierSegmentLengthRule(var maxLength: Int) : PrimaryViolationTierRule {
    override fun check(item: PrimaryViolationTier): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary violation tier. */
data class PrimaryViolationTierValidator(var rules: MutableList<PrimaryViolationTierRule>) {
    fun validate(item: PrimaryViolationTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryViolationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryViolationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryViolationTierValidator(): PrimaryViolationTierValidator {
    return PrimaryViolationTierValidator(
        mutableListOf<PrimaryViolationTierRule>(
            PrimaryViolationTierGraceDaysRangeRule(80, 120),
            PrimaryViolationTierRequiredCodeRule("primary violation tier needs this field"),
            PrimaryViolationTierRequiredSegmentRule("primary violation tier needs a value"),
            PrimaryViolationTierSegmentLengthRule(120),
        ),
    )
}
