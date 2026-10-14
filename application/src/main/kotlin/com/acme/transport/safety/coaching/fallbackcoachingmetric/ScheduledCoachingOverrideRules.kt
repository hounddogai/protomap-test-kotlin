package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.text.normalize

/** A rule a scheduled coaching override must pass. Check returns its problems, or none. */
interface ScheduledCoachingOverrideRule {
    fun check(item: ScheduledCoachingOverride): MutableList<String>
}

data class ScheduledCoachingOverrideCodePrefixRule(var prefix: String) : ScheduledCoachingOverrideRule {
    override fun check(item: ScheduledCoachingOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingOverrideRequiredRouteCodeRule(var message: String) : ScheduledCoachingOverrideRule {
    override fun check(item: ScheduledCoachingOverride): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingOverrideAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledCoachingOverrideRule {
    override fun check(item: ScheduledCoachingOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingOverrideRequiredNameRule(var message: String) : ScheduledCoachingOverrideRule {
    override fun check(item: ScheduledCoachingOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingOverrideRequiredCodeRule(var message: String) : ScheduledCoachingOverrideRule {
    override fun check(item: ScheduledCoachingOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCoachingOverrideActiveGuardRule(var limit: Int) : ScheduledCoachingOverrideRule {
    override fun check(item: ScheduledCoachingOverride): MutableList<String> {
        if (item.active && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled coaching override. */
data class ScheduledCoachingOverrideValidator(var rules: MutableList<ScheduledCoachingOverrideRule>) {
    fun validate(item: ScheduledCoachingOverride): MutableList<String> {
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

    fun requireValid(item: ScheduledCoachingOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCoachingOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCoachingOverrideValidator(): ScheduledCoachingOverrideValidator {
    return ScheduledCoachingOverrideValidator(
        mutableListOf<ScheduledCoachingOverrideRule>(
            ScheduledCoachingOverrideCodePrefixRule("OPS"),
            ScheduledCoachingOverrideRequiredRouteCodeRule("scheduled coaching override needs this field"),
            ScheduledCoachingOverrideAllowedCodeRule(mutableListOf<String>("weighted", "strict", "seasonal")),
            ScheduledCoachingOverrideRequiredNameRule("scheduled coaching override needs this field"),
            ScheduledCoachingOverrideRequiredCodeRule("scheduled coaching override needs an entry"),
            ScheduledCoachingOverrideActiveGuardRule(32),
        ),
    )
}
