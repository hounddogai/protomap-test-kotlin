package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a global zone tier must pass. Check returns its problems, or none. */
interface GlobalZoneTierRule {
    fun check(item: GlobalZoneTier): MutableList<String>
}

data class GlobalZoneTierOwnerIdLengthRule(var maxLength: Int) : GlobalZoneTierRule {
    override fun check(item: GlobalZoneTier): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTierAllowedOwnerIdRule(var allowed: MutableList<String>) : GlobalZoneTierRule {
    override fun check(item: GlobalZoneTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTierPinnedGuardRule(var limit: Int) : GlobalZoneTierRule {
    override fun check(item: GlobalZoneTier): MutableList<String> {
        if (item.pinned && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTierCodePrefixRule(var prefix: String) : GlobalZoneTierRule {
    override fun check(item: GlobalZoneTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTierRequiredCodeRule(var message: String) : GlobalZoneTierRule {
    override fun check(item: GlobalZoneTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneTierNameLengthRule(var maxLength: Int) : GlobalZoneTierRule {
    override fun check(item: GlobalZoneTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global zone tier. */
data class GlobalZoneTierValidator(var rules: MutableList<GlobalZoneTierRule>) {
    fun validate(item: GlobalZoneTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalZoneTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalZoneTier>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalZoneTierValidator(): GlobalZoneTierValidator {
    return GlobalZoneTierValidator(
        mutableListOf<GlobalZoneTierRule>(
            GlobalZoneTierOwnerIdLengthRule(1000),
            GlobalZoneTierAllowedOwnerIdRule(mutableListOf<String>("lenient", "tiered")),
            GlobalZoneTierPinnedGuardRule(1000),
            GlobalZoneTierCodePrefixRule("Q"),
            GlobalZoneTierRequiredCodeRule("global zone tier needs a value"),
            GlobalZoneTierNameLengthRule(80),
        ),
    )
}
