package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.text.normalize

/** A rule a roster claim must pass. Check returns its problems, or none. */
interface RosterClaimRule {
    fun check(item: RosterClaim): MutableList<String>
}

data class RosterClaimAllowedExternalRefRule(var allowed: MutableList<String>) : RosterClaimRule {
    override fun check(item: RosterClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RosterClaimCodeLengthRule(var maxLength: Int) : RosterClaimRule {
    override fun check(item: RosterClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterClaimCodePrefixRule(var prefix: String) : RosterClaimRule {
    override fun check(item: RosterClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterClaimRequiredNameRule(var message: String) : RosterClaimRule {
    override fun check(item: RosterClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterClaimVehicleIdLengthRule(var maxLength: Int) : RosterClaimRule {
    override fun check(item: RosterClaim): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster claim. */
data class RosterClaimValidator(var rules: MutableList<RosterClaimRule>) {
    fun validate(item: RosterClaim): MutableList<String> {
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

    fun requireValid(item: RosterClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterClaimValidator(): RosterClaimValidator {
    return RosterClaimValidator(
        mutableListOf<RosterClaimRule>(
            RosterClaimAllowedExternalRefRule(mutableListOf<String>("pending", "strict")),
            RosterClaimCodeLengthRule(1000),
            RosterClaimCodePrefixRule("X"),
            RosterClaimRequiredNameRule("roster claim needs an entry"),
            RosterClaimVehicleIdLengthRule(1000),
        ),
    )
}
