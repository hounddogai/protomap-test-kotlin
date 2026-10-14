package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.kit.text.normalize

/** A rule a attempt allocation must pass. Check returns its problems, or none. */
interface AttemptAllocationRule {
    fun check(item: AttemptAllocation): MutableList<String>
}

data class AttemptAllocationRequiredTitleRule(var message: String) : AttemptAllocationRule {
    override fun check(item: AttemptAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptAllocationReviewerLengthRule(var maxLength: Int) : AttemptAllocationRule {
    override fun check(item: AttemptAllocation): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptAllocationLaneLengthRule(var maxLength: Int) : AttemptAllocationRule {
    override fun check(item: AttemptAllocation): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptAllocationAllowedTitleRule(var allowed: MutableList<String>) : AttemptAllocationRule {
    override fun check(item: AttemptAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt allocation. */
data class AttemptAllocationValidator(var rules: MutableList<AttemptAllocationRule>) {
    fun validate(item: AttemptAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptAllocationValidator(): AttemptAllocationValidator {
    return AttemptAllocationValidator(
        mutableListOf<AttemptAllocationRule>(
            AttemptAllocationRequiredTitleRule("attempt allocation needs this field"),
            AttemptAllocationReviewerLengthRule(64),
            AttemptAllocationLaneLengthRule(80),
            AttemptAllocationAllowedTitleRule(mutableListOf<String>("legacy", "regional", "global")),
        ),
    )
}
