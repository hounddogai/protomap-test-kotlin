package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.text.normalize

/** A rule a capped cutoff milestone must pass. Check returns its problems, or none. */
interface CappedCutoffMilestoneRule {
    fun check(item: CappedCutoffMilestone): MutableList<String>
}

data class CappedCutoffMilestoneRequiredCodeRule(var message: String) : CappedCutoffMilestoneRule {
    override fun check(item: CappedCutoffMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffMilestoneAllowedCodeRule(var allowed: MutableList<String>) : CappedCutoffMilestoneRule {
    override fun check(item: CappedCutoffMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffMilestoneGraceDaysRangeRule(var min: Int, var max: Int) : CappedCutoffMilestoneRule {
    override fun check(item: CappedCutoffMilestone): MutableList<String> {
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

data class CappedCutoffMilestoneUpdatedAtRangeRule(var min: Int, var max: Int) : CappedCutoffMilestoneRule {
    override fun check(item: CappedCutoffMilestone): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffMilestoneCodeLengthRule(var maxLength: Int) : CappedCutoffMilestoneRule {
    override fun check(item: CappedCutoffMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped cutoff milestone. */
data class CappedCutoffMilestoneValidator(var rules: MutableList<CappedCutoffMilestoneRule>) {
    fun validate(item: CappedCutoffMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCutoffMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCutoffMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCutoffMilestoneValidator(): CappedCutoffMilestoneValidator {
    return CappedCutoffMilestoneValidator(
        mutableListOf<CappedCutoffMilestoneRule>(
            CappedCutoffMilestoneRequiredCodeRule("capped cutoff milestone needs this field"),
            CappedCutoffMilestoneAllowedCodeRule(mutableListOf<String>("capped")),
            CappedCutoffMilestoneGraceDaysRangeRule(120, 3600),
            CappedCutoffMilestoneUpdatedAtRangeRule(80, 1000),
            CappedCutoffMilestoneCodeLengthRule(86400),
        ),
    )
}
