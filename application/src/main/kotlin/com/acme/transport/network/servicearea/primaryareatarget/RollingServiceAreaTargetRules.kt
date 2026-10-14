package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.text.normalize

/** A rule a rolling service area target must pass. Check returns its problems, or none. */
interface RollingServiceAreaTargetRule {
    fun check(item: RollingServiceAreaTarget): MutableList<String>
}

data class RollingServiceAreaTargetCodePrefixRule(var prefix: String) : RollingServiceAreaTargetRule {
    override fun check(item: RollingServiceAreaTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingServiceAreaTargetRequiredCodeRule(var message: String) : RollingServiceAreaTargetRule {
    override fun check(item: RollingServiceAreaTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingServiceAreaTargetAllowedDockRule(
    var allowed: MutableList<String>,
) : RollingServiceAreaTargetRule {
    override fun check(item: RollingServiceAreaTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling service area target. */
data class RollingServiceAreaTargetValidator(var rules: MutableList<RollingServiceAreaTargetRule>) {
    fun validate(item: RollingServiceAreaTarget): MutableList<String> {
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

    fun requireValid(item: RollingServiceAreaTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingServiceAreaTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingServiceAreaTargetValidator(): RollingServiceAreaTargetValidator {
    return RollingServiceAreaTargetValidator(
        mutableListOf<RollingServiceAreaTargetRule>(
            RollingServiceAreaTargetCodePrefixRule("REF"),
            RollingServiceAreaTargetRequiredCodeRule("rolling service area target needs this field"),
            RollingServiceAreaTargetAllowedDockRule(mutableListOf<String>()),
        ),
    )
}
