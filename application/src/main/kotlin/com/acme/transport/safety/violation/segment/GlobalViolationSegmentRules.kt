package com.acme.transport.safety.violation.segment

/** A rule a global violation segment must pass. Check returns its problems, or none. */
interface GlobalViolationSegmentRule {
    fun check(item: GlobalViolationSegment): MutableList<String>
}

data class GlobalViolationSegmentRequiredCodeRule(var message: String) : GlobalViolationSegmentRule {
    override fun check(item: GlobalViolationSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationSegmentRequiredTitleRule(var message: String) : GlobalViolationSegmentRule {
    override fun check(item: GlobalViolationSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationSegmentCodeLengthRule(var maxLength: Int) : GlobalViolationSegmentRule {
    override fun check(item: GlobalViolationSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationSegmentScoreRangeRule(var min: Double, var max: Double) : GlobalViolationSegmentRule {
    override fun check(item: GlobalViolationSegment): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationSegmentCurrencyLengthRule(var maxLength: Int) : GlobalViolationSegmentRule {
    override fun check(item: GlobalViolationSegment): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global violation segment. */
data class GlobalViolationSegmentValidator(var rules: MutableList<GlobalViolationSegmentRule>) {
    fun validate(item: GlobalViolationSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalViolationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalViolationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalViolationSegmentValidator(): GlobalViolationSegmentValidator {
    return GlobalViolationSegmentValidator(
        mutableListOf<GlobalViolationSegmentRule>(
            GlobalViolationSegmentRequiredCodeRule("global violation segment needs this field"),
            GlobalViolationSegmentRequiredTitleRule("global violation segment needs this field"),
            GlobalViolationSegmentCodeLengthRule(3600),
            GlobalViolationSegmentScoreRangeRule(4.7, 715.4),
            GlobalViolationSegmentCurrencyLengthRule(80),
        ),
    )
}
