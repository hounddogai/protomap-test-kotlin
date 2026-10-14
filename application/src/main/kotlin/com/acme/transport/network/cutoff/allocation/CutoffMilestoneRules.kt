package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.text.normalize

/** A rule a cutoff milestone must pass. Check returns its problems, or none. */
interface CutoffMilestoneRule {
    fun check(item: CutoffMilestone): MutableList<String>
}

data class CutoffMilestoneCodePrefixRule(var prefix: String) : CutoffMilestoneRule {
    override fun check(item: CutoffMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffMilestoneAllowedExternalRefRule(var allowed: MutableList<String>) : CutoffMilestoneRule {
    override fun check(item: CutoffMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CutoffMilestoneRetriesRangeRule(var min: Int, var max: Int) : CutoffMilestoneRule {
    override fun check(item: CutoffMilestone): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffMilestoneNameLengthRule(var maxLength: Int) : CutoffMilestoneRule {
    override fun check(item: CutoffMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffMilestoneNotesLengthRule(var maxLength: Int) : CutoffMilestoneRule {
    override fun check(item: CutoffMilestone): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffMilestoneRequiredCodeRule(var message: String) : CutoffMilestoneRule {
    override fun check(item: CutoffMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff milestone. */
data class CutoffMilestoneValidator(var rules: MutableList<CutoffMilestoneRule>) {
    fun validate(item: CutoffMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffMilestoneValidator(): CutoffMilestoneValidator {
    return CutoffMilestoneValidator(
        mutableListOf<CutoffMilestoneRule>(
            CutoffMilestoneCodePrefixRule("X"),
            CutoffMilestoneAllowedExternalRefRule(mutableListOf<String>("seasonal")),
            CutoffMilestoneRetriesRangeRule(32, 255),
            CutoffMilestoneNameLengthRule(32),
            CutoffMilestoneNotesLengthRule(3600),
            CutoffMilestoneRequiredCodeRule("cutoff milestone needs this field"),
        ),
    )
}
