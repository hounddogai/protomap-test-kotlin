package com.acme.transport.safety.incident.variance

import com.acme.transport.kit.text.normalize

/** A rule a regional incident claim must pass. Check returns its problems, or none. */
interface RegionalIncidentClaimRule {
    fun check(item: RegionalIncidentClaim): MutableList<String>
}

data class RegionalIncidentClaimNameLengthRule(var maxLength: Int) : RegionalIncidentClaimRule {
    override fun check(item: RegionalIncidentClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentClaimTrailerIdLengthRule(var maxLength: Int) : RegionalIncidentClaimRule {
    override fun check(item: RegionalIncidentClaim): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentClaimCodePrefixRule(var prefix: String) : RegionalIncidentClaimRule {
    override fun check(item: RegionalIncidentClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentClaimRequiredNameRule(var message: String) : RegionalIncidentClaimRule {
    override fun check(item: RegionalIncidentClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentClaimAllowedNameRule(var allowed: MutableList<String>) : RegionalIncidentClaimRule {
    override fun check(item: RegionalIncidentClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RegionalIncidentClaimCodeLengthRule(var maxLength: Int) : RegionalIncidentClaimRule {
    override fun check(item: RegionalIncidentClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional incident claim. */
data class RegionalIncidentClaimValidator(var rules: MutableList<RegionalIncidentClaimRule>) {
    fun validate(item: RegionalIncidentClaim): MutableList<String> {
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

    fun requireValid(item: RegionalIncidentClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalIncidentClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalIncidentClaimValidator(): RegionalIncidentClaimValidator {
    return RegionalIncidentClaimValidator(
        mutableListOf<RegionalIncidentClaimRule>(
            RegionalIncidentClaimNameLengthRule(255),
            RegionalIncidentClaimTrailerIdLengthRule(64),
            RegionalIncidentClaimCodePrefixRule("ACM"),
            RegionalIncidentClaimRequiredNameRule("regional incident claim needs an entry"),
            RegionalIncidentClaimAllowedNameRule(mutableListOf<String>("lenient", "seasonal", "adaptive")),
            RegionalIncidentClaimCodeLengthRule(32),
        ),
    )
}
