package com.acme.transport.customs.broker.lenientbrokerestimate

/** A rule a adaptive broker assignment must pass. Check returns its problems, or none. */
interface AdaptiveBrokerAssignmentRule {
    fun check(item: AdaptiveBrokerAssignment): MutableList<String>
}

data class AdaptiveBrokerAssignmentCodeLengthRule(var maxLength: Int) : AdaptiveBrokerAssignmentRule {
    override fun check(item: AdaptiveBrokerAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerAssignmentRequiredTitleRule(var message: String) : AdaptiveBrokerAssignmentRule {
    override fun check(item: AdaptiveBrokerAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerAssignmentEndsAtRangeRule(var min: Int, var max: Int) : AdaptiveBrokerAssignmentRule {
    override fun check(item: AdaptiveBrokerAssignment): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveBrokerAssignmentEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveBrokerAssignmentRule {
    override fun check(item: AdaptiveBrokerAssignment): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerAssignmentStopCountRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveBrokerAssignmentRule {
    override fun check(item: AdaptiveBrokerAssignment): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class AdaptiveBrokerAssignmentRequiredCodeRule(var message: String) : AdaptiveBrokerAssignmentRule {
    override fun check(item: AdaptiveBrokerAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive broker assignment. */
data class AdaptiveBrokerAssignmentValidator(var rules: MutableList<AdaptiveBrokerAssignmentRule>) {
    fun validate(item: AdaptiveBrokerAssignment): MutableList<String> {
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

    fun requireValid(item: AdaptiveBrokerAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveBrokerAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveBrokerAssignmentValidator(): AdaptiveBrokerAssignmentValidator {
    return AdaptiveBrokerAssignmentValidator(
        mutableListOf<AdaptiveBrokerAssignmentRule>(
            AdaptiveBrokerAssignmentCodeLengthRule(255),
            AdaptiveBrokerAssignmentRequiredTitleRule("adaptive broker assignment needs this field"),
            AdaptiveBrokerAssignmentEndsAtRangeRule(16, 86400),
            AdaptiveBrokerAssignmentEtaMinutesRangeRule(80, 255),
            AdaptiveBrokerAssignmentStopCountRangeRule(80, 255),
            AdaptiveBrokerAssignmentRequiredCodeRule("adaptive broker assignment needs an entry"),
        ),
    )
}
