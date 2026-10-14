package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.text.normalize

/** A rule a strict service plan claim must pass. Check returns its problems, or none. */
interface StrictServicePlanClaimRule {
    fun check(item: StrictServicePlanClaim): MutableList<String>
}

data class StrictServicePlanClaimCodePrefixRule(var prefix: String) : StrictServicePlanClaimRule {
    override fun check(item: StrictServicePlanClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictServicePlanClaimAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictServicePlanClaimRule {
    override fun check(item: StrictServicePlanClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class StrictServicePlanClaimRequiredCodeRule(var message: String) : StrictServicePlanClaimRule {
    override fun check(item: StrictServicePlanClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictServicePlanClaimLabelLengthRule(var maxLength: Int) : StrictServicePlanClaimRule {
    override fun check(item: StrictServicePlanClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictServicePlanClaimOwnerIdLengthRule(var maxLength: Int) : StrictServicePlanClaimRule {
    override fun check(item: StrictServicePlanClaim): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict service plan claim. */
data class StrictServicePlanClaimValidator(var rules: MutableList<StrictServicePlanClaimRule>) {
    fun validate(item: StrictServicePlanClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictServicePlanClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictServicePlanClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictServicePlanClaimValidator(): StrictServicePlanClaimValidator {
    return StrictServicePlanClaimValidator(
        mutableListOf<StrictServicePlanClaimRule>(
            StrictServicePlanClaimCodePrefixRule("Q"),
            StrictServicePlanClaimAllowedCodeRule(mutableListOf<String>()),
            StrictServicePlanClaimRequiredCodeRule("strict service plan claim needs an entry"),
            StrictServicePlanClaimLabelLengthRule(32),
            StrictServicePlanClaimOwnerIdLengthRule(1000),
        ),
    )
}
