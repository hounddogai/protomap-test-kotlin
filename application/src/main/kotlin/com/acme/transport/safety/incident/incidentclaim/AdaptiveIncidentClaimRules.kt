package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.text.normalize

/** A rule a adaptive incident claim must pass. Check returns its problems, or none. */
interface AdaptiveIncidentClaimRule {
    fun check(item: AdaptiveIncidentClaim): MutableList<String>
}

data class AdaptiveIncidentClaimRequiredUnitRule(var message: String) : AdaptiveIncidentClaimRule {
    override fun check(item: AdaptiveIncidentClaim): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIncidentClaimNameLengthRule(var maxLength: Int) : AdaptiveIncidentClaimRule {
    override fun check(item: AdaptiveIncidentClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIncidentClaimAllowedNameRule(var allowed: MutableList<String>) : AdaptiveIncidentClaimRule {
    override fun check(item: AdaptiveIncidentClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIncidentClaimRequiredCodeRule(var message: String) : AdaptiveIncidentClaimRule {
    override fun check(item: AdaptiveIncidentClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIncidentClaimCodePrefixRule(var prefix: String) : AdaptiveIncidentClaimRule {
    override fun check(item: AdaptiveIncidentClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive incident claim. */
data class AdaptiveIncidentClaimValidator(var rules: MutableList<AdaptiveIncidentClaimRule>) {
    fun validate(item: AdaptiveIncidentClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveIncidentClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveIncidentClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveIncidentClaimValidator(): AdaptiveIncidentClaimValidator {
    return AdaptiveIncidentClaimValidator(
        mutableListOf<AdaptiveIncidentClaimRule>(
            AdaptiveIncidentClaimRequiredUnitRule("adaptive incident claim needs an entry"),
            AdaptiveIncidentClaimNameLengthRule(255),
            AdaptiveIncidentClaimAllowedNameRule(mutableListOf<String>()),
            AdaptiveIncidentClaimRequiredCodeRule("adaptive incident claim needs a value"),
            AdaptiveIncidentClaimCodePrefixRule("ACM"),
        ),
    )
}
