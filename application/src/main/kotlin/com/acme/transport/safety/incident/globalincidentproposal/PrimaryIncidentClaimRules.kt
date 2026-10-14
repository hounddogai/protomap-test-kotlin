package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.kit.text.normalize

/** A rule a primary incident claim must pass. Check returns its problems, or none. */
interface PrimaryIncidentClaimRule {
    fun check(item: PrimaryIncidentClaim): MutableList<String>
}

data class PrimaryIncidentClaimAllowedRegionRule(var allowed: MutableList<String>) : PrimaryIncidentClaimRule {
    override fun check(item: PrimaryIncidentClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentClaimApprovedGuardRule(var limit: Double) : PrimaryIncidentClaimRule {
    override fun check(item: PrimaryIncidentClaim): MutableList<String> {
        if (item.approved && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentClaimCodeLengthRule(var maxLength: Int) : PrimaryIncidentClaimRule {
    override fun check(item: PrimaryIncidentClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentClaimTitleLengthRule(var maxLength: Int) : PrimaryIncidentClaimRule {
    override fun check(item: PrimaryIncidentClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentClaimCodePrefixRule(var prefix: String) : PrimaryIncidentClaimRule {
    override fun check(item: PrimaryIncidentClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryIncidentClaimRequiredSegmentRule(var message: String) : PrimaryIncidentClaimRule {
    override fun check(item: PrimaryIncidentClaim): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary incident claim. */
data class PrimaryIncidentClaimValidator(var rules: MutableList<PrimaryIncidentClaimRule>) {
    fun validate(item: PrimaryIncidentClaim): MutableList<String> {
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

    fun requireValid(item: PrimaryIncidentClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryIncidentClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryIncidentClaimValidator(): PrimaryIncidentClaimValidator {
    return PrimaryIncidentClaimValidator(
        mutableListOf<PrimaryIncidentClaimRule>(
            PrimaryIncidentClaimAllowedRegionRule(mutableListOf<String>()),
            PrimaryIncidentClaimApprovedGuardRule(2.2),
            PrimaryIncidentClaimCodeLengthRule(255),
            PrimaryIncidentClaimTitleLengthRule(1000),
            PrimaryIncidentClaimCodePrefixRule("ACM"),
            PrimaryIncidentClaimRequiredSegmentRule("primary incident claim needs this field"),
        ),
    )
}
