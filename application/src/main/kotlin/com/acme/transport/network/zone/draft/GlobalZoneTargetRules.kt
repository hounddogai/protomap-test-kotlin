package com.acme.transport.network.zone.draft

import com.acme.transport.kit.text.normalize

/** A rule a global zone target must pass. Check returns its problems, or none. */
interface GlobalZoneTargetRule {
    fun check(item: GlobalZoneTarget): MutableList<String>
}

data class GlobalZoneTargetRequiredLocaleRule(var message: String) : GlobalZoneTargetRule {
    override fun check(item: GlobalZoneTarget): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTargetCodePrefixRule(var prefix: String) : GlobalZoneTargetRule {
    override fun check(item: GlobalZoneTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTargetAllowedDriverIdRule(var allowed: MutableList<String>) : GlobalZoneTargetRule {
    override fun check(item: GlobalZoneTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global zone target. */
data class GlobalZoneTargetValidator(var rules: MutableList<GlobalZoneTargetRule>) {
    fun validate(item: GlobalZoneTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalZoneTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalZoneTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalZoneTargetValidator(): GlobalZoneTargetValidator {
    return GlobalZoneTargetValidator(
        mutableListOf<GlobalZoneTargetRule>(
            GlobalZoneTargetRequiredLocaleRule("global zone target needs an entry"),
            GlobalZoneTargetCodePrefixRule("OPS"),
            GlobalZoneTargetAllowedDriverIdRule(mutableListOf<String>("manual", "legacy")),
        ),
    )
}
