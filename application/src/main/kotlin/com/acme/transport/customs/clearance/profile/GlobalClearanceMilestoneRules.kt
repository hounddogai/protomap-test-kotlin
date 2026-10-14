package com.acme.transport.customs.clearance.profile

import com.acme.transport.kit.text.normalize

/** A rule a global clearance milestone must pass. Check returns its problems, or none. */
interface GlobalClearanceMilestoneRule {
    fun check(item: GlobalClearanceMilestone): MutableList<String>
}

data class GlobalClearanceMilestoneSequenceRangeRule(
    var min: Int,
    var max: Int,
) : GlobalClearanceMilestoneRule {
    override fun check(item: GlobalClearanceMilestone): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalClearanceMilestoneRequiredCodeRule(var message: String) : GlobalClearanceMilestoneRule {
    override fun check(item: GlobalClearanceMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalClearanceMilestoneCodePrefixRule(var prefix: String) : GlobalClearanceMilestoneRule {
    override fun check(item: GlobalClearanceMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalClearanceMilestoneAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalClearanceMilestoneRule {
    override fun check(item: GlobalClearanceMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global clearance milestone. */
data class GlobalClearanceMilestoneValidator(var rules: MutableList<GlobalClearanceMilestoneRule>) {
    fun validate(item: GlobalClearanceMilestone): MutableList<String> {
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

    fun requireValid(item: GlobalClearanceMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalClearanceMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalClearanceMilestoneValidator(): GlobalClearanceMilestoneValidator {
    return GlobalClearanceMilestoneValidator(
        mutableListOf<GlobalClearanceMilestoneRule>(
            GlobalClearanceMilestoneSequenceRangeRule(32, 1000),
            GlobalClearanceMilestoneRequiredCodeRule("global clearance milestone needs an entry"),
            GlobalClearanceMilestoneCodePrefixRule("X"),
            GlobalClearanceMilestoneAllowedCodeRule(mutableListOf<String>("manual", "lenient")),
        ),
    )
}
