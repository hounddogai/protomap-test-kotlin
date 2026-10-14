package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.text.normalize

/** A rule a rolling dispute group must pass. Check returns its problems, or none. */
interface RollingDisputeGroupRule {
    fun check(item: RollingDisputeGroup): MutableList<String>
}

data class RollingDisputeGroupCodePrefixRule(var prefix: String) : RollingDisputeGroupRule {
    override fun check(item: RollingDisputeGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingDisputeGroupAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : RollingDisputeGroupRule {
    override fun check(item: RollingDisputeGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RollingDisputeGroupTitleLengthRule(var maxLength: Int) : RollingDisputeGroupRule {
    override fun check(item: RollingDisputeGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDisputeGroupRequiredTrailerIdRule(var message: String) : RollingDisputeGroupRule {
    override fun check(item: RollingDisputeGroup): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDisputeGroupExternalRefLengthRule(var maxLength: Int) : RollingDisputeGroupRule {
    override fun check(item: RollingDisputeGroup): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dispute group. */
data class RollingDisputeGroupValidator(var rules: MutableList<RollingDisputeGroupRule>) {
    fun validate(item: RollingDisputeGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDisputeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDisputeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDisputeGroupValidator(): RollingDisputeGroupValidator {
    return RollingDisputeGroupValidator(
        mutableListOf<RollingDisputeGroupRule>(
            RollingDisputeGroupCodePrefixRule("ACM"),
            RollingDisputeGroupAllowedExternalRefRule(mutableListOf<String>("fallback", "weighted", "lenient")),
            RollingDisputeGroupTitleLengthRule(120),
            RollingDisputeGroupRequiredTrailerIdRule("rolling dispute group needs a value"),
            RollingDisputeGroupExternalRefLengthRule(64),
        ),
    )
}
