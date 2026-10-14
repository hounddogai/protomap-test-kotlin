package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a locker plan must pass. Check returns its problems, or none. */
interface LockerPlanRule {
    fun check(item: LockerPlan): MutableList<String>
}

data class LockerPlanChannelLengthRule(var maxLength: Int) : LockerPlanRule {
    override fun check(item: LockerPlan): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerPlanAllowedTitleRule(var allowed: MutableList<String>) : LockerPlanRule {
    override fun check(item: LockerPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class LockerPlanRequiredTitleRule(var message: String) : LockerPlanRule {
    override fun check(item: LockerPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerPlanLocaleLengthRule(var maxLength: Int) : LockerPlanRule {
    override fun check(item: LockerPlan): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker plan. */
data class LockerPlanValidator(var rules: MutableList<LockerPlanRule>) {
    fun validate(item: LockerPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerPlanValidator(): LockerPlanValidator {
    return LockerPlanValidator(
        mutableListOf<LockerPlanRule>(
            LockerPlanChannelLengthRule(32),
            LockerPlanAllowedTitleRule(mutableListOf<String>("manual")),
            LockerPlanRequiredTitleRule("locker plan needs an entry"),
            LockerPlanLocaleLengthRule(80),
        ),
    )
}
