package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.kit.text.normalize

/** A rule a regional redelivery claim must pass. Check returns its problems, or none. */
interface RegionalRedeliveryClaimRule {
    fun check(item: RegionalRedeliveryClaim): MutableList<String>
}

data class RegionalRedeliveryClaimCodeLengthRule(var maxLength: Int) : RegionalRedeliveryClaimRule {
    override fun check(item: RegionalRedeliveryClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryClaimLabelLengthRule(var maxLength: Int) : RegionalRedeliveryClaimRule {
    override fun check(item: RegionalRedeliveryClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryClaimAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalRedeliveryClaimRule {
    override fun check(item: RegionalRedeliveryClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional redelivery claim. */
data class RegionalRedeliveryClaimValidator(var rules: MutableList<RegionalRedeliveryClaimRule>) {
    fun validate(item: RegionalRedeliveryClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalRedeliveryClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRedeliveryClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRedeliveryClaimValidator(): RegionalRedeliveryClaimValidator {
    return RegionalRedeliveryClaimValidator(
        mutableListOf<RegionalRedeliveryClaimRule>(
            RegionalRedeliveryClaimCodeLengthRule(255),
            RegionalRedeliveryClaimLabelLengthRule(255),
            RegionalRedeliveryClaimAllowedCodeRule(mutableListOf<String>("batched")),
        ),
    )
}
