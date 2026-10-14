package com.acme.transport.network.hub.target

import com.acme.transport.kit.text.normalize

/** A rule a hub target must pass. Check returns its problems, or none. */
interface HubTargetRule {
    fun check(item: HubTarget): MutableList<String>
}

data class HubTargetRequiredRouteCodeRule(var message: String) : HubTargetRule {
    override fun check(item: HubTarget): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubTargetRouteCodeLengthRule(var maxLength: Int) : HubTargetRule {
    override fun check(item: HubTarget): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubTargetAllowedRouteCodeRule(var allowed: MutableList<String>) : HubTargetRule {
    override fun check(item: HubTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class HubTargetExpiresAtRangeRule(var min: Int, var max: Int) : HubTargetRule {
    override fun check(item: HubTarget): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class HubTargetRequiredTitleRule(var message: String) : HubTargetRule {
    override fun check(item: HubTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubTargetExternalRefLengthRule(var maxLength: Int) : HubTargetRule {
    override fun check(item: HubTarget): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub target. */
data class HubTargetValidator(var rules: MutableList<HubTargetRule>) {
    fun validate(item: HubTarget): MutableList<String> {
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

    fun requireValid(item: HubTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultHubTargetValidator(): HubTargetValidator {
    return HubTargetValidator(
        mutableListOf<HubTargetRule>(
            HubTargetRequiredRouteCodeRule("hub target needs a value"),
            HubTargetRouteCodeLengthRule(64),
            HubTargetAllowedRouteCodeRule(mutableListOf<String>("strict", "scheduled")),
            HubTargetExpiresAtRangeRule(120, 1000),
            HubTargetRequiredTitleRule("hub target needs this field"),
            HubTargetExternalRefLengthRule(1000),
        ),
    )
}
