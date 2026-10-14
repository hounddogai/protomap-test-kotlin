package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.text.normalize

/** A rule a broker assignment must pass. Check returns its problems, or none. */
interface BrokerAssignmentRule {
    fun check(item: BrokerAssignment): MutableList<String>
}

data class BrokerAssignmentRequiredCodeRule(var message: String) : BrokerAssignmentRule {
    override fun check(item: BrokerAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerAssignmentScoreRangeRule(var min: Double, var max: Double) : BrokerAssignmentRule {
    override fun check(item: BrokerAssignment): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class BrokerAssignmentAllowedCodeRule(var allowed: MutableList<String>) : BrokerAssignmentRule {
    override fun check(item: BrokerAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker assignment. */
data class BrokerAssignmentValidator(var rules: MutableList<BrokerAssignmentRule>) {
    fun validate(item: BrokerAssignment): MutableList<String> {
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

    fun requireValid(item: BrokerAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerAssignmentValidator(): BrokerAssignmentValidator {
    return BrokerAssignmentValidator(
        mutableListOf<BrokerAssignmentRule>(
            BrokerAssignmentRequiredCodeRule("broker assignment needs a value"),
            BrokerAssignmentScoreRangeRule(9.5, 2359.2),
            BrokerAssignmentAllowedCodeRule(mutableListOf<String>("batched", "weighted", "primary")),
        ),
    )
}
