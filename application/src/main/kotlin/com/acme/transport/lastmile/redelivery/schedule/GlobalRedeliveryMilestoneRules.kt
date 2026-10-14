package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.text.normalize

/** A rule a global redelivery milestone must pass. Check returns its problems, or none. */
interface GlobalRedeliveryMilestoneRule {
    fun check(item: GlobalRedeliveryMilestone): MutableList<String>
}

data class GlobalRedeliveryMilestoneChecksumLengthRule(var maxLength: Int) : GlobalRedeliveryMilestoneRule {
    override fun check(item: GlobalRedeliveryMilestone): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryMilestoneAllowedNameRule(
    var allowed: MutableList<String>,
) : GlobalRedeliveryMilestoneRule {
    override fun check(item: GlobalRedeliveryMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryMilestoneRequiredNameRule(var message: String) : GlobalRedeliveryMilestoneRule {
    override fun check(item: GlobalRedeliveryMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryMilestoneRequiredCodeRule(var message: String) : GlobalRedeliveryMilestoneRule {
    override fun check(item: GlobalRedeliveryMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRedeliveryMilestoneTitleLengthRule(var maxLength: Int) : GlobalRedeliveryMilestoneRule {
    override fun check(item: GlobalRedeliveryMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global redelivery milestone. */
data class GlobalRedeliveryMilestoneValidator(var rules: MutableList<GlobalRedeliveryMilestoneRule>) {
    fun validate(item: GlobalRedeliveryMilestone): MutableList<String> {
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

    fun requireValid(item: GlobalRedeliveryMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRedeliveryMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRedeliveryMilestoneValidator(): GlobalRedeliveryMilestoneValidator {
    return GlobalRedeliveryMilestoneValidator(
        mutableListOf<GlobalRedeliveryMilestoneRule>(
            GlobalRedeliveryMilestoneChecksumLengthRule(120),
            GlobalRedeliveryMilestoneAllowedNameRule(mutableListOf<String>("adaptive", "weighted", "fallback")),
            GlobalRedeliveryMilestoneRequiredNameRule("global redelivery milestone needs a value"),
            GlobalRedeliveryMilestoneRequiredCodeRule("global redelivery milestone needs an entry"),
            GlobalRedeliveryMilestoneTitleLengthRule(255),
        ),
    )
}
