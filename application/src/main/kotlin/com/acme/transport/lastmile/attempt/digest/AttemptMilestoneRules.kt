package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.text.normalize

/** A rule a attempt milestone must pass. Check returns its problems, or none. */
interface AttemptMilestoneRule {
    fun check(item: AttemptMilestone): MutableList<String>
}

data class AttemptMilestoneAllowedDockRule(var allowed: MutableList<String>) : AttemptMilestoneRule {
    override fun check(item: AttemptMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class AttemptMilestoneTitleLengthRule(var maxLength: Int) : AttemptMilestoneRule {
    override fun check(item: AttemptMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptMilestoneCodeLengthRule(var maxLength: Int) : AttemptMilestoneRule {
    override fun check(item: AttemptMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptMilestoneRequiredDockRule(var message: String) : AttemptMilestoneRule {
    override fun check(item: AttemptMilestone): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptMilestoneDockLengthRule(var maxLength: Int) : AttemptMilestoneRule {
    override fun check(item: AttemptMilestone): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptMilestoneRequiredDescriptionRule(var message: String) : AttemptMilestoneRule {
    override fun check(item: AttemptMilestone): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt milestone. */
data class AttemptMilestoneValidator(var rules: MutableList<AttemptMilestoneRule>) {
    fun validate(item: AttemptMilestone): MutableList<String> {
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

    fun requireValid(item: AttemptMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptMilestoneValidator(): AttemptMilestoneValidator {
    return AttemptMilestoneValidator(
        mutableListOf<AttemptMilestoneRule>(
            AttemptMilestoneAllowedDockRule(mutableListOf<String>("legacy", "capped", "primary")),
            AttemptMilestoneTitleLengthRule(86400),
            AttemptMilestoneCodeLengthRule(80),
            AttemptMilestoneRequiredDockRule("attempt milestone needs this field"),
            AttemptMilestoneDockLengthRule(32),
            AttemptMilestoneRequiredDescriptionRule("attempt milestone needs a value"),
        ),
    )
}
