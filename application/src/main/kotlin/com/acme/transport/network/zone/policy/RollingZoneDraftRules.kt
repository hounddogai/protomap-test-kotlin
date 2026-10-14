package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.normalize

/** A rule a rolling zone draft must pass. Check returns its problems, or none. */
interface RollingZoneDraftRule {
    fun check(item: RollingZoneDraft): MutableList<String>
}

data class RollingZoneDraftNameLengthRule(var maxLength: Int) : RollingZoneDraftRule {
    override fun check(item: RollingZoneDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneDraftAllowedNameRule(var allowed: MutableList<String>) : RollingZoneDraftRule {
    override fun check(item: RollingZoneDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneDraftRequiredNameRule(var message: String) : RollingZoneDraftRule {
    override fun check(item: RollingZoneDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneDraftCodeLengthRule(var maxLength: Int) : RollingZoneDraftRule {
    override fun check(item: RollingZoneDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling zone draft. */
data class RollingZoneDraftValidator(var rules: MutableList<RollingZoneDraftRule>) {
    fun validate(item: RollingZoneDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingZoneDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingZoneDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingZoneDraftValidator(): RollingZoneDraftValidator {
    return RollingZoneDraftValidator(
        mutableListOf<RollingZoneDraftRule>(
            RollingZoneDraftNameLengthRule(86400),
            RollingZoneDraftAllowedNameRule(mutableListOf<String>("batched")),
            RollingZoneDraftRequiredNameRule("rolling zone draft needs a value"),
            RollingZoneDraftCodeLengthRule(16),
        ),
    )
}
